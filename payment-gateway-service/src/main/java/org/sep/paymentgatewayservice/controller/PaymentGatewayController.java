package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.RedirectionResponse;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.MerchantPaymentMethods;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PaymentGatewayController implements PaymentGatewayServiceApi, PaymentMethodRegistrationApi {

    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentGatewayController(final PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @Override
    public ResponseEntity<RedirectionResponse> preparePayment(final PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.preparePayment(paymentRequest));
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(final PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.createPayment(paymentRequest));
    }

    @Override
    public ResponseEntity<Map<Long, String>> retrievePaymentMethodsRegistrationUrls(final MerchantPaymentMethods merchantPaymentMethods) {
        return ResponseEntity.ok(this.paymentGatewayService.retrievePaymentMethodsRegistrationUrl(merchantPaymentMethods));
    }

    @Override
    public ResponseEntity<List<SubscriptionPlan>> retrieveSubscriptionPlans(final String merchantId) {
        return ResponseEntity.ok(this.paymentGatewayService.retrieveSubscriptionPlans(merchantId));
    }

    @Override
    public ResponseEntity<SubscriptionResponse> createSubscription(final SubscriptionRequest subscriptionRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.createSubscription(subscriptionRequest));
    }

    @Override
    public ResponseEntity<Void> registerPaymentMethod(final PaymentMethodData paymentMethodData) {
        this.paymentGatewayService.registerPaymentMethod(paymentMethodData);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Boolean> notifyMerchantIsRegistered(final NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto) {
        return ResponseEntity.ok(this.paymentGatewayService.notifyMerchantIsRegistered(notifyPaymentMethodRegistrationDto));
    }
}