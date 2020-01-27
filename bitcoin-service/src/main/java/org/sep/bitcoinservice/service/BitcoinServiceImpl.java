package org.sep.bitcoinservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.bitcoinservice.exceptions.NoMerchantFoundException;
import org.sep.bitcoinservice.model.*;
import org.sep.paymentgatewayservice.method.api.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.method.api.PaymentStatus;
import org.sep.paymentgatewayservice.payment.entity.*;
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

    private static final String SERVICE_NAME = "Bitcoin";
    private static final String HTTPS_PREFIX = "https://";
    @Value("${ip.address}")
    private String SERVER_ADDRESS;
    @Value("${server.port}")
    private String SERVER_PORT;
    private final RestTemplate restTemplate;
    private final MerchantService merchantService;
    private final TransactionService transactionService;
    private final PaymentMethodRegistrationApi paymentMethodRegistrationApi;

    @Autowired
    public BitcoinServiceImpl(final RestTemplate restTemplate, final MerchantService merchantService, final TransactionService transactionService, final PaymentMethodRegistrationApi paymentMethodRegistrationApi) {
        this.restTemplate = restTemplate;
        this.merchantService = merchantService;
        this.transactionService = transactionService;
        this.paymentMethodRegistrationApi = paymentMethodRegistrationApi;
    }

    @Override
    public PaymentResponse createOrder(final PaymentRequest paymentRequest) {
        try {

            final CGRequest cgRequest = CGRequest.builder()
                    .price_amount(paymentRequest.getPrice())
                    .price_currency("BTC")
                    .receive_currency("DO_NOT_CONVERT")
                    .title(paymentRequest.getMerchantName() + " : " + paymentRequest.getItem())
                    .description(paymentRequest.getDescription())
                    .success_url(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/success_payment?orderId=" + paymentRequest.getMerchantOrderId())
                    .cancel_url(HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/cancel_payment?orderId=" + paymentRequest.getMerchantOrderId())
                    .build();

            final Merchant merchant = this.merchantService.findByMerchantId(paymentRequest.getMerchantId());
            if (merchant == null){
                log.error("Merchant with id: {} not found", paymentRequest.getMerchantId());
                throw new NoMerchantFoundException(paymentRequest.getMerchantId());
            }
            log.info("Merchant with id:{} is retrieved", merchant.getMerchantId());

            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + merchant.getToken());
            final HttpEntity<CGRequest> requestEntity = new HttpEntity<>(cgRequest, headers);

            log.info("Payment request(merchant: {}, item: {}) is sent", paymentRequest.getMerchantId(), paymentRequest.getItem());
            final ResponseEntity<CGResponse> response = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders", HttpMethod.POST, requestEntity, CGResponse.class);
            final CGResponse cgResponse = response.getBody();
            log.info("Payment request (merchant: {}, item: {}) is approved", paymentRequest.getMerchantId(), paymentRequest.getItem());

            final Transaction transaction = Transaction.builder()
                    .merchant(merchant)
                    .orderId(paymentRequest.getMerchantOrderId())
                    .cgId(cgResponse.getId())
                    .item(paymentRequest.getItem())
                    .price(cgResponse.getPrice_amount())
                    .priceCurrency(cgResponse.getPrice_currency())
                    .timestamp(cgResponse.getCreated_at())
                    .status(MerchantOrderStatus.IN_PROGRESS)
                    .returnUrl(paymentRequest.getReturnUrl())
                    .build();
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {})  is saved", paymentRequest.getMerchantId(), paymentRequest.getItem());

            final CGCheckout cgCheckout = CGCheckout.builder()
                    .pay_currency("BTC")
                    .build();
            final HttpEntity<CGCheckout> checkoutRequestEntity = new HttpEntity<>(cgCheckout, headers);
            log.info("Checkout transaction (cgId: {})", cgResponse.getId());
            final ResponseEntity<CGResponse> checkoutResponse = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders/" + cgResponse.getId() + "/checkout", HttpMethod.POST, checkoutRequestEntity, CGResponse.class);
            final CGResponse checkoutCGResponse = checkoutResponse.getBody();

            final PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .paymentUrl(checkoutCGResponse.getPayment_url())
                    .status(CreatePaymentStatus.CREATED)
                    .build();
            return paymentMethodResponse;
        } catch (final HttpClientErrorException e) {
            log.error("Payment request (merchant: {}, item: {}) is denied", paymentRequest.getMerchantId(), paymentRequest.getItem());
            final PaymentResponse paymentMethodResponse = PaymentResponse.builder()
                    .status(CreatePaymentStatus.ERROR)
                    .paymentUrl(paymentRequest.getReturnUrl())
                    .build();
            return paymentMethodResponse;
        }
    }

    @Override
    public String completePayment(final PaymentCompleteRequest request) {
        final Transaction transaction = this.transactionService.findByOrderId(request.getOrderId());
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            transaction.setStatus(MerchantOrderStatus.FINISHED);
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {}) is finished and updated", transaction.getMerchant().getMerchantId(), transaction.getItem());
        } else {
            transaction.setStatus(MerchantOrderStatus.CANCELED);
            this.transactionService.save(transaction);
            log.info("Transaction (merchant: {}, item: {}) is canceled and updated", transaction.getMerchant().getMerchantId(), transaction.getItem());
        }
        return transaction.getReturnUrl();
    }

    @Override
    public MerchantOrderStatus getOrderStatus(String orderId) {
        log.info("Retrieve transaction with order id: {}", orderId);
        Transaction transaction = this.transactionService.findByOrderId(orderId);
        log.info("Transaction status sent back to gateway");
        return transaction.getStatus();
    }

    @Override
    public String retrieveMerchantRegistrationUrl(final String merchantId) {
        log.info("Bitcoin registration page is sent");
        return HTTPS_PREFIX + this.SERVER_ADDRESS + ":" + this.SERVER_PORT + "/registration?merchantId=" + merchantId;
    }

    @Override
    public String registerMerchant(final Merchant merchant) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + merchant.getToken());
            final HttpEntity requestEntity = new HttpEntity(headers);
            this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders", HttpMethod.GET, requestEntity, Void.class);
            if (merchantService.findByMerchantId(merchant.getMerchantId()) != null) {
                log.error("Merchant with id: {} already exist", merchant.getMerchantId());
                return "Merchant id already exist";
            }
            this.merchantService.save(merchant);
            log.info("Merchant with id: {} is created", merchant.getMerchantId());
            NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto = NotifyPaymentMethodRegistrationDto.builder()
                    .merchantId(merchant.getMerchantId())
                    .methodName(SERVICE_NAME)
                    .build();
            log.info("Notify gateway service");
            Boolean flag = this.paymentMethodRegistrationApi.notifyMerchantIsRegistered(notifyPaymentMethodRegistrationDto).getBody();
            if (flag){
                return "You have registered on Bitcoin service successfully!";
            }else{
                return "Error while registering on Bitcoin service";
            }
        }catch (final HttpClientErrorException e){
            log.error("Merchant token: {} doesn't exist", merchant.getToken());
            return "Merchant with this token doesn't exist on CoinGate";
        }
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void updateTransaction() {
        log.info("Start transaction checking");
        List<Transaction> transactionsInProgress = this.transactionService.findByStatus(MerchantOrderStatus.IN_PROGRESS);
        for (Transaction transaction : transactionsInProgress){
            log.info("Check transaction with cgId: {}", transaction.getCgId());
            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + transaction.getMerchant().getToken());
            final HttpEntity requestEntity = new HttpEntity(headers);

            log.info("Transaction request(cgId: {}) is sent", transaction.getCgId());
            final ResponseEntity<CGResponse> response = this.restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders/"+ transaction.getCgId(), HttpMethod.GET, requestEntity, CGResponse.class);
            final CGResponse cgResponse = response.getBody();
            if (cgResponse.getStatus().equals("paid")){
                log.info("Transaction (merchant: {}, item: {}) status changed to paid", transaction.getMerchant().getMerchantId(), transaction.getItem());
                transaction.setStatus(MerchantOrderStatus.FINISHED);
                this.transactionService.save(transaction);
                log.info("Transaction (merchant: {}, item: {}) successfully updated", transaction.getMerchant().getMerchantId(), transaction.getItem());
            }else if(cgResponse.getStatus().equals("invalid") || cgResponse.getStatus().equals("expired") || cgResponse.getStatus().equals("canceled")){
                log.info("Transaction (merchant: {}, item: {}) status changed to canceled", transaction.getMerchant().getMerchantId(), transaction.getItem());
                transaction.setStatus(MerchantOrderStatus.CANCELED);
                this.transactionService.save(transaction);
                log.info("Transaction (merchant: {}, item: {}) successfully updated", transaction.getMerchant().getMerchantId(), transaction.getItem());
            }
        }
    }
}
