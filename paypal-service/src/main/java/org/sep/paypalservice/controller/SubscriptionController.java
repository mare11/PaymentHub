package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.method.api.SubscriptionApi;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionPlan;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionResponse;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SubscriptionController implements SubscriptionApi {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(final SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public ResponseEntity<SubscriptionResponse> createSubscription(final SubscriptionRequest subscriptionRequest) {
        return ResponseEntity.ok(this.subscriptionService.createSubscription(subscriptionRequest));
    }

    @Override
    public ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(final String merchantId) {
        return ResponseEntity.ok(this.subscriptionService.retrieveSubscriptionPlans(merchantId));
    }

    @PostMapping(value = "/subscription_transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionDto> getPaymentTransaction(@RequestBody final CompleteDto completeDto) {
        return ResponseEntity.ok(this.subscriptionService.completeSubscriptionTransaction(completeDto));
    }
}