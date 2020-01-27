package org.sep.paymentgatewayservice.method.api;

import org.sep.paymentgatewayservice.payment.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "paypal-service", configuration = FeignConfiguration.class)
public interface SubscriptionApi {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody SubscriptionRequest subscriptionRequest);

    @GetMapping(value = "/{merchantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(@PathVariable String merchantId);

    @PostMapping(value = "/subscription/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubscriptionCancelResponse> cancelSubscription(@RequestBody final SubscriptionCancelRequest subscriptionCancelRequest);
}