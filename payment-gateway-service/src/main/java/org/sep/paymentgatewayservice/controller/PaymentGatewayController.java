package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodData;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRegistrationApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentGatewayController implements PaymentGatewayServiceApi, PaymentMethodRegistrationApi {

    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @Override
    public ResponseEntity<PaymentResponse> preparePayment(PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.preparePayment(paymentRequest));
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.paymentGatewayService.createPayment(paymentRequest));
    }

    @Override
    public ResponseEntity<Void> registerPaymentMethod(PaymentMethodData paymentMethodData) {
        this.paymentGatewayService.registerPaymentMethod(paymentMethodData);
        return ResponseEntity.ok().build();
    }
}