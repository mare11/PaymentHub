package org.sep.paymentgatewayservice.api;

import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.SellerPaymentMethods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "payment-gateway-service", configuration = FeignConfiguration.class)
public interface PaymentGatewayServiceApi {

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/payment_methods/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> registerPaymentMethods(@RequestBody SellerPaymentMethods sellerPaymentMethods);

    @GetMapping(value = "/subscription/{merchantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(@PathVariable String merchantId);

    @PostMapping(value = "/subscription/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody final SubscriptionRequest subscriptionRequest);
}