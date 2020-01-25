package org.sep.bitcoinservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bitcoinservice.model.*;
import org.sep.bitcoinservice.repository.TransactionIdRespository;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.CreatePaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class BitcoinServiceImpl implements BitcoinService {

    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private final RestTemplate restTemplate;
    private final MerchantService merchantService;
    private final TransactionService transactionService;
    private final TransactionIdRespository transactionIdRespository;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;

    @Autowired
    public BitcoinServiceImpl(final RestTemplate restTemplate, final MerchantService merchantService, final TransactionService transactionService, final TransactionIdRespository transactionIdRespository, final PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.restTemplate = restTemplate;
        this.merchantService = merchantService;
        this.transactionService = transactionService;
        this.transactionIdRespository = transactionIdRespository;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public PaymentResponse createOrder(final PaymentRequest paymentRequest) {
        try {
            TransactionId transactionId = new TransactionId();
            transactionId = this.transactionIdRespository.save(transactionId);
            log.info("Transaction id is created");

            final CGRequest cgRequest = CGRequest.builder()
                    .price_amount(paymentRequest.getPrice())
                    .receive_currency("DO_NOT_CONVERT")
                    .title(paymentRequest.getSellerName() + " : " + paymentRequest.getItem())
                    .description(paymentRequest.getDescription())
                    .success_url(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/success_payment?orderId=" + transactionId.getId())
                    .cancel_url(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/cancel_payment?orderId=" + transactionId.getId())
                    .order_id(transactionId.getId())
                    .build();

            if (paymentRequest.getPriceCurrency() != null) {
                cgRequest.setPrice_currency(paymentRequest.getPriceCurrency());
            } else {
                cgRequest.setPrice_currency("BTC");
            }

            final Merchant merchant = this.merchantService.findByIssn(paymentRequest.getSellerIssn());
            log.info("Merchant with issn:{} is retrieved", merchant.getIssn());

            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + merchant.getToken());
            final HttpEntity<CGRequest> requestEntity = new HttpEntity<>(cgRequest, headers);

            log.info("Payment request(merchant: {}, item: {}) is sent", paymentRequest.getSellerIssn(), paymentRequest.getItem());
            final ResponseEntity<CGResponse> response = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders", HttpMethod.POST, requestEntity, CGResponse.class);
            final CGResponse cgResponse = response.getBody();
            log.info("Payment request (merchant: {}, item: {}) is approved", paymentRequest.getSellerIssn(), paymentRequest.getItem());

            final Transaction transaction = Transaction.builder()
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

            final CGCheckout cgCheckout = CGCheckout.builder()
                    .pay_currency("BTC")
                    .build();
            final HttpEntity<CGCheckout> checkoutRequestEntity = new HttpEntity<>(cgCheckout, headers);
            log.info("Checkout transaction (cgId: {})", cgResponse.getId());
            final ResponseEntity<CGResponse> checkoutResponse = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders/" + cgResponse.getId() + "/checkout", HttpMethod.POST, checkoutRequestEntity, CGResponse.class);
            final CGResponse checkoutCGResponse = checkoutResponse.getBody();

            final PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .paymentUrl(checkoutCGResponse.getPayment_url())
                    .orderId(checkoutCGResponse.getId().toString())
                    .status(CreatePaymentStatus.CREATED)
                    .build();
            return paymentMethodResponse;
        } catch (final HttpClientErrorException e) {
            log.error("Payment request (merchant: {}, item: {}) is denied", paymentRequest.getSellerIssn(), paymentRequest.getItem());
            final PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .status(CreatePaymentStatus.ERROR)
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .build();
            return paymentMethodResponse;
        }
    }

    @Override
    public String completePayment(final PaymentCompleteRequest request) {
        final Transaction transaction = this.transactionService.findByOrderId(Long.parseLong(request.getOrderId()));
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
    public String retrieveSellerRegistrationUrl(final String issn) {
        log.info("Bitcoin registration page is sent");
        return HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/registration?issn=" + issn;
    }

    @Override
    public String registerSeller(final Merchant merchant) {
        this.merchantService.save(merchant);
        log.info("Merchant with issn: {} is created", merchant.getIssn());
        return this.paymentMethodRegistrationApi.proceedToNextPaymentMethod(merchant.getIssn()).getBody();
    }

    @Scheduled(fixedDelay = 30000)
    public void updateTransaction() {
        log.info("Start transaction checking");
        List<Transaction> newTransactions = this.transactionService.findByStatus(TransactionStatus.NEW);
        for (Transaction transaction : newTransactions){
            log.info("Check transaction with cgId: {}", transaction.getCgId());
            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + transaction.getMerchant().getToken());
            final HttpEntity requestEntity = new HttpEntity(headers);

            log.info("Transaction request(cgId: {}) is sent", transaction.getCgId());
            final ResponseEntity<CGResponse> response = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders/"+ transaction.getCgId(), HttpMethod.GET, requestEntity, CGResponse.class);
            final CGResponse cgResponse = response.getBody();
            if (cgResponse.getStatus().equals("paid")){
                log.info("Transaction (merchant: {}, item: {}) status changed to paid", transaction.getMerchant().getIssn(), transaction.getItem());
                transaction.setStatus(TransactionStatus.FINISHED);
                this.transactionService.save(transaction);
                log.info("Transaction (merchant: {}, item: {}) successfully updated", transaction.getMerchant().getIssn(), transaction.getItem());
            }else if(cgResponse.getStatus().equals("invalid") || cgResponse.getStatus().equals("expired") || cgResponse.getStatus().equals("canceled")){
                log.info("Transaction (merchant: {}, item: {}) status changed to canceled", transaction.getMerchant().getIssn(), transaction.getItem());
                transaction.setStatus(TransactionStatus.CANCELED);
                this.transactionService.save(transaction);
                log.info("Transaction (merchant: {}, item: {}) successfully updated", transaction.getMerchant().getIssn(), transaction.getItem());
            }
        }
    }
}
