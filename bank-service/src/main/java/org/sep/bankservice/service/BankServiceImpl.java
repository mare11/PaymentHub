package org.sep.bankservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bankservice.exception.InvalidDataException;
import org.sep.bankservice.exception.MerchantNotFoundException;
import org.sep.bankservice.model.*;
import org.sep.bankservice.repository.MerchantRepository;
import org.sep.bankservice.repository.TransactionRepository;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
import org.sep.paymentgatewayservice.payment.entity.NotifyPaymentMethodRegistrationDto;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

import static org.sep.bankservice.ApplicationStartupListener.SERVICE_NAME;
import static org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus.CREATED;
import static org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus.ERROR;
import static org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus.*;

@Slf4j
@Service
public class BankServiceImpl implements BankService {

    @Value("${acquirer.host}")
    private String acquirerHost;
    @Value("${acquirer.port}")
    private String acquirerPort;
    @Value("${ip.address}")
    private String serverAddress;
    @Value("${server.port}")
    private String serverPort;
    private static final String HTTPS_PREFIX = "https://";
    private final RestTemplate restTemplate;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;

    @Autowired
    public BankServiceImpl(final RestTemplate restTemplate, final MerchantRepository merchantRepository, final TransactionRepository transactionRepository, final PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.restTemplate = restTemplate;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) {
        this.assertAllNotNull(paymentRequest, paymentRequest.getMerchantId(), paymentRequest.getMerchantOrderId(),
                paymentRequest.getItem(), paymentRequest.getPrice(), paymentRequest.getReturnUrl());
        final MerchantEntity merchant = this.getMerchant(paymentRequest.getMerchantId());

        final TransactionRequest transactionRequest = TransactionRequest.builder()
                .merchantId(merchant.getBankMerchantId())
                .merchantPassword(merchant.getBankMerchantPassword())
                .merchantOrderId(paymentRequest.getMerchantOrderId())
                .merchantTimestamp(LocalDateTime.now())
                .item(paymentRequest.getItem())
                .amount(paymentRequest.getPrice())
                .description(paymentRequest.getDescription())
                .successUrl(HTTPS_PREFIX + this.serverAddress + ":" + this.serverPort + "/success_payment?orderId=" + paymentRequest.getMerchantOrderId())
                .errorUrl(HTTPS_PREFIX + this.serverAddress + ":" + this.serverPort + "/cancel_payment?orderId=" + paymentRequest.getMerchantOrderId())
                .build();

        log.info("Sending request (merchantId: {}, item: {}) to acquirer", merchant.getMerchantId(), paymentRequest.getItem());
        final HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(transactionRequest);
        ResponseEntity<TransactionResponse> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(getAcquirerUrl(), HttpMethod.POST, requestEntity, TransactionResponse.class);
        } catch (Exception e) {
            log.error("Got exception when calling acquirer: {}", e.getMessage());
            return PaymentResponse.builder()
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .status(ERROR)
                    .build();
        }
        final TransactionResponse response = responseEntity.getBody();
        if (response == null || !response.isSuccess()) {
            log.error("Wrong response from acquirer (merchantId: {}, orderId: {})", merchant.getMerchantId(), paymentRequest.getMerchantOrderId());
            return PaymentResponse.builder()
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .status(ERROR)
                    .build();
        }

        log.info("Response from acquirer (paymentId: {}, paymentUrl: {})", response.getPaymentId(), response.getPaymentUrl());
        final TransactionEntity transaction = TransactionEntity.builder()
                .orderId(paymentRequest.getMerchantOrderId())
                .bankTransactionId(response.getPaymentId())
                .item(paymentRequest.getItem())
                .status(IN_PROGRESS)
                .price(paymentRequest.getPrice())
                .timestamp(transactionRequest.getMerchantTimestamp())
                .returnUrl(paymentRequest.getReturnUrl())
                .merchant(merchant)
                .build();

        this.transactionRepository.save(transaction);
        log.info("Transaction (item: {}, price: {}, timestamp: {}, merchantId: {}) saved",
                transaction.getItem(), transaction.getPrice(), transaction.getTimestamp(), merchant.getMerchantId());

        return PaymentResponse.builder()
                .paymentUrl(response.getPaymentUrl())
                .status(CREATED)
                .build();
    }

    @Override
    public String completePayment(final PaymentCompleteRequest paymentCompleteRequest) {
        TransactionEntity transaction = getTransactionByOrderId(paymentCompleteRequest.getOrderId());
        transaction.setStatus(paymentCompleteRequest.getStatus() == PaymentStatus.SUCCESS ? FINISHED : CANCELED);
        this.transactionRepository.save(transaction);
        log.info("Transaction status updated ({})", transaction.getStatus());
        return transaction.getReturnUrl();
    }

    @Override
    public String retrieveMerchantRegistrationUrl(String merchantId) {
        return HTTPS_PREFIX + this.serverAddress + ":" + this.serverPort + "/registration?merchantId=" + merchantId;
    }

    @Override
    public TransactionResponse registerMerchant(final Merchant merchant) {
        if (merchantRepository.findByBankMerchantId(merchant.getBankMerchantId()) != null) {
            log.error("Merchant (bank merchant id: {}) already exists!", merchant.getBankMerchantId());
            return TransactionResponse.builder()
                    .success(false)
                    .message("Merchant with entered credentials already exists!")
                    .build();
        }
        final HttpEntity<Merchant> requestEntity = new HttpEntity<>(merchant);
        ResponseEntity<Boolean> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(getAcquirerUrl().concat("/client"), HttpMethod.POST, requestEntity, Boolean.class);
        } catch (Exception e) {
            log.error("Got exception when calling acquirer: {}", e.getMessage());
            return TransactionResponse.builder()
                    .success(false)
                    .message("An unexpected error occurred!")
                    .build();
        }

        Boolean exists = responseEntity.getBody();
        if (exists == null || !exists) {
            return TransactionResponse.builder()
                    .success(false)
                    .message("Entered credentials are not valid within the bank!")
                    .build();
        }

        Boolean success = this.paymentMethodRegistrationApi.notifyMerchantIsRegistered(
                NotifyPaymentMethodRegistrationDto.builder()
                        .merchantId(merchant.getMerchantId())
                        .methodName(SERVICE_NAME)
                        .build()
        ).getBody();

        if (success != null && success) {
            MerchantEntity merchantEntity = MerchantEntity.builder()
                    .merchantId(merchant.getMerchantId())
                    .bankMerchantId(merchant.getBankMerchantId())
                    .bankMerchantPassword(merchant.getBankMerchantPassword())
                    .build();
            this.merchantRepository.save(merchantEntity);
            log.info("Merchant (merchantId: {}) registered!", merchantEntity.getMerchantId());
            return TransactionResponse.builder()
                    .success(true)
                    .message("Merchant is successfully registered!")
                    .build();
        } else {
            return TransactionResponse.builder()
                    .success(false)
                    .message("An unexpected error occurred!")
                    .build();
        }
    }

    @Override
    public MerchantOrderStatus getOrderStatus(String orderId) {
        return getTransactionByOrderId(orderId).getStatus();
    }

    private void assertAllNotNull(final Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private MerchantEntity getMerchant(final String merchantId) {
        final MerchantEntity merchant = this.merchantRepository.findByMerchantId(merchantId);
        if (merchant == null) {
            log.error("No merchant with id: {}", merchantId);
            throw new MerchantNotFoundException(merchantId);
        }
        log.info("Found merchant with id: {}", merchantId);
        return merchant;
    }

    private TransactionEntity getTransactionByOrderId(String orderId) {
        final TransactionEntity transaction = this.transactionRepository.findByOrderId(orderId);
        if (transaction == null) {
            throw new InvalidDataException();
        }
        return transaction;
    }

    public String getAcquirerUrl() {
        return HTTPS_PREFIX.concat(acquirerHost).concat(":").concat(acquirerPort).concat("/acquirer");
    }

}
