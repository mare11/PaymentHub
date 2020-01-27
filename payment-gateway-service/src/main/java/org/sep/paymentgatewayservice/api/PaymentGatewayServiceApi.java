package org.sep.paymentgatewayservice.api;

import org.sep.paymentgatewayservice.method.api.MerchantOrderStatus;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.MerchantPaymentMethods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(value = "payment-gateway-service", configuration = FeignConfiguration.class)
public interface PaymentGatewayServiceApi {

    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RedirectionResponse> preparePayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/payment_methods", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<Long, String>> retrievePaymentMethodsRegistrationUrls(@RequestBody MerchantPaymentMethods merchantPaymentMethods);

    @GetMapping(value = "/subscription/{merchantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(@PathVariable String merchantId);

    @PostMapping(value = "/subscription/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody final SubscriptionRequest subscriptionRequest);

    @GetMapping(value = "/order_status/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MerchantOrderStatus> checkOrderStatus(@PathVariable String orderId);
}