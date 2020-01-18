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
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.methodapi.PaymentStatus;
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
    public BankServiceImpl(RestTemplate restTemplate, MerchantRepository merchantRepository, TransactionRepository transactionRepository, PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.restTemplate = restTemplate;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        assertAllNotNull(paymentRequest, paymentRequest.getSellerIssn(), paymentRequest.getItem(),
                paymentRequest.getPrice(), paymentRequest.getReturnUrl());
        Merchant merchant = getMerchant(paymentRequest.getSellerIssn());

        TransactionRequest transactionRequest = TransactionRequest.builder()
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
        HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(transactionRequest);
        ResponseEntity<TransactionResponse> responseEntity = this.restTemplate.exchange(HTTPS_PREFIX + ACQUIRER_URL,
                HttpMethod.POST, requestEntity, TransactionResponse.class);

        TransactionResponse response = responseEntity.getBody();
        if (response == null) {
            log.error("Wrong response from acquirer (merchantId: {}, item: {})", merchant.getMerchantId(), paymentRequest.getItem());
            return PaymentResponse.builder()
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .status(CreatePaymentStatus.ERROR)
                    .build();
        }

        log.info("Response from acquirer (paymentId: {}, paymentUrl: {})", response.getPaymentId(), response.getPaymentUrl());
        Transaction transaction = Transaction.builder()
                .orderId(response.getPaymentId())
                .item(paymentRequest.getItem())
                .status(NEW)
                .price(paymentRequest.getPrice())
                .timestamp(transactionRequest.getMerchantTimestamp())
                .returnUrl(paymentRequest.getReturnUrl())
                .merchant(merchant)
                .build();

        transactionRepository.save(transaction);
        log.info("Transaction (item: {}, price: {}, timestamp: {}, merchantId: {}) saved",
                transaction.getItem(), transaction.getPrice(), transaction.getTimestamp(), merchant.getMerchantId());

        return PaymentResponse.builder()
                .orderId(response.getPaymentId())
                .paymentUrl(response.getPaymentUrl())
                .status(CreatePaymentStatus.CREATED)
                .build();
    }

    @Override
    public String completePayment(PaymentCompleteRequest paymentCompleteRequest) {
        Transaction transaction = transactionRepository.findByOrderId(paymentCompleteRequest.getOrderId());
        if (transaction == null) {
            throw new InvalidDataException();
        }
        transaction.setStatus(paymentCompleteRequest.getStatus() == PaymentStatus.SUCCESS ? FINISHED : CANCELED);
        transactionRepository.save(transaction);
        log.info("Transaction status updated ({})", transaction.getStatus());
        return transaction.getReturnUrl();
    }

    @Override
    public String retrieveSellerRegistrationUrl(String issn) {
        return HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/registration?issn=" + issn;
    }

    @Override
    public String registerSeller(Merchant merchant) {
        merchantRepository.save(merchant);
        log.info("Merchant (issn: {}, merchantId: {}) registered", merchant.getIssn(), merchant.getMerchantId());
        return paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchant.getIssn()).getBody();
    }

    private void assertAllNotNull(Object... objects) {
        if (Stream.of(objects).anyMatch(Objects::isNull)) {
            throw new InvalidDataException();
        }
    }

    private Merchant getMerchant(String issn) {
        Merchant merchant = merchantRepository.findByIssn(issn);
        if (merchant == null) {
            log.error("No merchant for issn: {}", issn);
            throw new MerchantNotFoundException(issn);
        }
        log.info("Found merchant with issn: {}", issn);
        return merchant;
    }
}
