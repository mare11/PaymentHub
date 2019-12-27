package org.sep.bitcoinservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bitcoinservice.exceptions.NoMerchantFoundException;
import org.sep.bitcoinservice.model.*;
import org.sep.bitcoinservice.repository.TransactionIdRespository;
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.methodapi.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class BitcoinServiceImpl implements BitcoinService {

    private static final String HTTP_PREFIX = "http://";
    @Value("${server.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private final RestTemplate restTemplate;
    private final MerchantService merchantService;
    private final TransactionService transactionService;
    private final TransactionIdRespository transactionIdRespository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;

    @Autowired
    public BitcoinServiceImpl(RestTemplate restTemplate, MerchantService merchantService, TransactionService transactionService, TransactionIdRespository transactionIdRespository, PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.restTemplate = restTemplate;
        this.merchantService = merchantService;
        this.transactionService = transactionService;
        this.transactionIdRespository = transactionIdRespository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public PaymentResponse createOrder(PaymentRequest paymentRequest) {
        try {
            TransactionId transactionId = new TransactionId();
            transactionId = this.transactionIdRespository.save(transactionId);
            log.info("Transaction id is created");

            CGRequest cgRequest = CGRequest.builder()
                    .price_amount(paymentRequest.getPrice())
                    .receive_currency("DO_NOT_CONVERT")
                    .title(paymentRequest.getSellerName() + " : " + paymentRequest.getItem())
                    .description(paymentRequest.getDescription())
                    .success_url(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/success_payment?orderId=" + transactionId.getId())
                    .cancel_url(HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/cancel_payment?orderId=" + transactionId.getId())
                    .order_id(transactionId.getId())
                    .build();

            if (paymentRequest.getPriceCurrency() != null) {
                cgRequest.setPrice_currency(paymentRequest.getPriceCurrency());
            } else {
                cgRequest.setPrice_currency("BTC");
            }

            Merchant merchant = this.merchantService.findByIssn(paymentRequest.getSellerIssn());
            log.info("Merchant with issn:{} is retrieved", merchant.getIssn());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + merchant.getToken());
            HttpEntity<CGRequest> requestEntity = new HttpEntity<>(cgRequest, headers);

            log.info("Payment request(merchant: {}, item: {}) is sent", paymentRequest.getSellerIssn(), paymentRequest.getItem());
            ResponseEntity<CGResponse> response = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders", HttpMethod.POST, requestEntity, CGResponse.class);
            CGResponse cgResponse = response.getBody();
            log.info("Payment request (merchant: {}, item: {}) is approved", paymentRequest.getSellerIssn(), paymentRequest.getItem());

            Transaction transaction = Transaction.builder()
                    .merchant(merchant)
                    .orderId(cgResponse.getOrder_id())
                    .cgId(cgResponse.getId())
                    .item(paymentRequest.getItem())
                    .price(cgResponse.getPrice_amount())
                    .priceCurrency(cgResponse.getPrice_currency())
                    .timestamp(cgResponse.getCreated_at())
                    .status(TransactionStatus.NEW)
                    .returnUrl(paymentRequest.getReturnUrl())
                    .build();
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {})  is saved", paymentRequest.getSellerIssn(), paymentRequest.getItem());

            PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .paymentUrl(cgResponse.getPayment_url())
                    .orderId(cgResponse.getId().toString())
                    .status(CreatePaymentStatus.CREATED)
                    .build();
            return paymentMethodResponse;
        } catch (HttpClientErrorException e) {
            log.error("Payment request (merchant: {}, item: {}) is denied", paymentRequest.getSellerIssn(), paymentRequest.getItem());
            PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .status(CreatePaymentStatus.ERROR)
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .build();
            return paymentMethodResponse;
        }
    }

    @Override
    public String completePayment(PaymentCompleteRequest request) {
        Transaction transaction = this.transactionService.findByOrderId(Long.parseLong(request.getOrderId()));
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            transaction.setStatus(TransactionStatus.FINISHED);
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {}) is finished and updated", transaction.getMerchant().getIssn(), transaction.getItem());
        } else {
            transaction.setStatus(TransactionStatus.CANCELED);
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {}) is canceled and updated", transaction.getMerchant().getIssn(), transaction.getItem());
        }
        return transaction.getReturnUrl();
    }

    @Override
    public String retrieveSellerRegistrationUrl(String issn) {
        log.info("Bitcoin registration page is sent");
        return HTTP_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/registration?issn=" + issn;
    }

    @Override
    public String registerSeller(Merchant merchant) {
        this.merchantService.save(merchant);
        log.info("Merchant with issn: {} is created", merchant.getIssn());
        return this.paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchant.getIssn()).getBody();
    }
}
