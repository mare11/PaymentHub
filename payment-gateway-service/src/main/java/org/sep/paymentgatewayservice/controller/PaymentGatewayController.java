package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.method.api.PaymentMethodData;
import org.sep.paymentgatewayservice.method.api.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.payment.entity.*;
import org.sep.paymentgatewayservice.seller.api.SellerPaymentMethods;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PaymentGatewayController implements PaymentGatewayServiceApi, PaymentMethodRegistrationApi {

    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentGatewayController(final PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @Override
    public ResponseEntity<PaymentResponse> preparePayment(final PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.preparePayment(paymentRequest));
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(final PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.createPayment(paymentRequest));
    }

    @Override
    public ResponseEntity<String> registerPaymentMethods(final SellerPaymentMethods sellerPaymentMethods) {
        return ResponseEntity.ok(this.paymentGatewayService.registerSellerInPaymentMethod(sellerPaymentMethods));
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
    public ResponseEntity<String> proceedToNextPaymentMethod(final String merchantId) {
        return ResponseEntity.ok(this.paymentGatewayService.proceedToNextPaymentMethod(merchantId));
    }
}