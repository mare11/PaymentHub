package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.exceptions.NoMerchantFoundException;
import org.sep.bitcoinservice.model.*;
import org.sep.paymentgatewayservice.methodapi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class BitcoinServiceImpl implements BitcoinService {

    private RestTemplate restTemplate;
    private MerchantService merchantService;
    private TransactionService transactionService;

    @Autowired
    public BitcoinServiceImpl(RestTemplate restTemplate, MerchantService merchantService, TransactionService transactionService){
        this.restTemplate = restTemplate;
        this.merchantService = merchantService;
        this.transactionService = transactionService;
    }

    @Override
    public PaymentMethodResponse createOrder(PaymentMethodRequest paymentMethodRequest) {
        try {
            CGRequest cgRequest = CGRequest.builder()
                                           .price_amount(paymentMethodRequest.getPrice())
                                           .price_currency(paymentMethodRequest.getPriceCurrency())
                                           .receive_currency("DO_NOT_CONVERT")
                                           .title(paymentMethodRequest.getSellerName() + " : " + paymentMethodRequest.getItem())
                                           .description(paymentMethodRequest.getDescription())
                                           .cancel_url(paymentMethodRequest.getCancelUrl())
                                           .success_url(paymentMethodRequest.getSuccessUrl())
                                           .build();

            Merchant merchant = this.merchantService.findByIssn(paymentMethodRequest.getSellerIssn());
            if (merchant == null){
                throw new NoMerchantFoundException(paymentMethodRequest.getSellerIssn());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + merchant.getToken());
            HttpEntity<CGRequest> requestEntity = new HttpEntity<>(cgRequest, headers);
            ResponseEntity<CGResponse> response = restTemplate.exchange("https://api-sandbox.coingate.com/v2/orders", HttpMethod.POST, requestEntity, CGResponse.class);
            CGResponse cgResponse =  response.getBody();

            Transaction transaction = Transaction.builder()
                                                 .merchant(merchant)
                                                 .orderId(cgResponse.getId())
                                                 .price(cgResponse.getPrice_amount())
                                                 .priceCurrency(cgResponse.getPrice_currency())
                                                 .timestamp(cgResponse.getCreated_at())
                                                 .status(TransactionStatus.NEW)
                                                 .build();
            this.transactionService.save(transaction);

            PaymentMethodResponse paymentMethodResponse = PaymentMethodResponse.builder()
                                                                               .paymentUrl(cgResponse.getPayment_url())
                                                                               .orderId(cgResponse.getId().toString())
                                                                               .status(CreatePaymentStatus.CREATED)
                                                                               .build();
            return paymentMethodResponse;
        } catch (HttpClientErrorException e) {
            PaymentMethodResponse paymentMethodResponse = PaymentMethodResponse.builder()
                                                                               .status(CreatePaymentStatus.ERROR)
                                                                               .build();
            return paymentMethodResponse;
        }
    }

    @Override
    public void completePayment(PaymentCompleteRequest request) {
        Transaction transaction = this.transactionService.findByOrderId(Long.parseLong(request.getOrderId()));
        if (request.getStatus() == PaymentStatus.SUCCESS){
            transaction.setStatus(TransactionStatus.FINISHED);
            transactionService.save(transaction);
        }else{
            transaction.setStatus(TransactionStatus.CANCELED);
            transactionService.save(transaction);
        }
    }
}
