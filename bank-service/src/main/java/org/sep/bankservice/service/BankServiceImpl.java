package org.sep.bankservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bankservice.exception.InvalidDataException;
import org.sep.bankservice.exception.MerchantNotFoundException;
import org.sep.bankservice.model.Merchant;
import org.sep.bankservice.model.Transaction;
import org.sep.bankservice.model.TransactionRequest;
import org.sep.bankservice.model.TransactionResponse;
import org.sep.bankservice.repository.MerchantRepository;
import org.sep.bankservice.repository.TransactionRepository;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
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

import static org.sep.bankservice.model.TransactionStatus.*;

@Slf4j
@Service
public class BankServiceImpl implements BankService {

    private static final String ACQUIRER_URL = "localhost:9991/prepare";
    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
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
        this.assertAllNotNull(paymentRequest, paymentRequest.getSellerIssn(), paymentRequest.getItem(),
                paymentRequest.getPrice(), paymentRequest.getReturnUrl());
        final Merchant merchant = this.getMerchant(paymentRequest.getSellerIssn());

        final TransactionRequest transactionRequest = TransactionRequest.builder()
                .merchantId(merchant.getMerchantId())
                .merchantPassword(merchant.getMerchantPassword())
//                .merchantOrderId(orderId)
                .merchantTimestamp(LocalDateTime.now())
                .item(paymentRequest.getItem())
                .amount(paymentRequest.getPrice())
                .description(paymentRequest.getDescription())
                .successUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/success_payment?orderId=")
                .errorUrl(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/cancel_payment?orderId=")
                .build();

        log.info("Sending request (merchantId: {}, item: {}) to acquirer", merchant.getMerchantId(), paymentRequest.getItem());
        final HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(transactionRequest);
        final ResponseEntity<TransactionResponse> responseEntity = this.restTemplate.exchange(HTTPS_PREFIX + ACQUIRER_URL,
                HttpMethod.POST, requestEntity, TransactionResponse.class);

        final TransactionResponse response = responseEntity.getBody();
        if (response == null) {
            log.error("Wrong response from acquirer (merchantId: {}, item: {})", merchant.getMerchantId(), paymentRequest.getItem());
            return PaymentResponse.builder()
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .status(CreatePaymentStatus.ERROR)
                    .build();
        }

        log.info("Response from acquirer (paymentId: {}, paymentUrl: {})", response.getPaymentId(), response.getPaymentUrl());
        final Transaction transaction = Transaction.builder()
                .orderId(response.getPaymentId())
                .item(paymentRequest.getItem())
                .status(NEW)
                .price(paymentRequest.getPrice())
                .timestamp(transactionRequest.getMerchantTimestamp())
                .returnUrl(paymentRequest.getReturnUrl())
                .merchant(merchant)
                .build();

        this.transactionRepository.save(transaction);
        log.info("Transaction (item: {}, price: {}, timestamp: {}, merchantId: {}) saved",
                transaction.getItem(), transaction.getPrice(), transaction.getTimestamp(), merchant.getMerchantId());

        return PaymentResponse.builder()
                .orderId(response.getPaymentId())
                .paymentUrl(response.getPaymentUrl())
                .status(CreatePaymentStatus.CREATED)
                .build();
    }

    @Override
    public String completePayment(final PaymentCompleteRequest paymentCompleteRequest) {
        final Transaction transaction = this.transactionRepository.findByOrderId(paymentCompleteRequest.getOrderId());
        if (transaction == null) {
            throw new InvalidDataException();
        }
        transaction.setStatus(paymentCompleteRequest.getStatus() == PaymentStatus.SUCCESS ? FINISHED : CANCELED);
        this.transactionRepository.save(transaction);
        log.info("Transaction status updated ({})", transaction.getStatus());
        return transaction.getReturnUrl();
    }

    @Override
    public String retrieveSellerRegistrationUrl(final String issn) {
        return HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/registration?issn=" + issn;
    }

    @Override
    public String registerSeller(final Merchant merchant) {
        this.merchantRepository.save(merchant);
        log.info("Merchant (issn: {}, merchantId: {}) registered", merchant.getIssn(), merchant.getMerchantId());
        return this.paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchant.getIssn()).getBody();
    }

    private void assertAllNotNull(final Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private Merchant getMerchant(final String issn) {
        final Merchant merchant = this.merchantRepository.findByIssn(issn);
        if (merchant == null) {
            log.error("No merchant for issn: {}", issn);
            throw new MerchantNotFoundException(issn);
        }
        log.info("Found merchant with issn: {}", issn);
        return merchant;
    }
}
