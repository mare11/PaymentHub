package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.method.api.SubscriptionStatus;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionCancelRequest;
import org.sep.paymentgatewayservice.payment.entity.SubscriptionCancelResponse;
import org.sep.paymentgatewayservice.seller.api.MerchantServiceApi;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/subscription")
public class SubscriptionController {

    private final MerchantServiceApi merchantServiceApi;
    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public SubscriptionController(final MerchantServiceApi merchantServiceApi, final PaymentGatewayService paymentGatewayService) {
        this.merchantServiceApi = merchantServiceApi;
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionResponse> subscribe(@RequestBody final MerchantRequest merchantRequest) {
        return this.merchantServiceApi.prepareSubscription(merchantRequest);
    }

    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionCancelResponse> cancelSubscription(@RequestBody final SubscriptionCancelRequest subscriptionCancelRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.cancelSubscription(subscriptionCancelRequest));
    }

    @GetMapping(value = "/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionStatus> getSubscriptionStatus(@PathVariable final String subscriptionId) {
        return ResponseEntity.ok(this.paymentGatewayService.checkSubscriptionStatus(subscriptionId));
    }
}