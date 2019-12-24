package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PaymentController implements PaymentMethodApi {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(URI baseUrl, PaymentRequest paymentRequest) {
        PaymentResponse paymentMethodResponse = this.paymentService.createPayment(paymentRequest);
        return ResponseEntity.ok(paymentMethodResponse);
    }

    @Override
    public ResponseEntity<String> completePayment(PaymentCompleteRequest paymentCompleteRequest) {
        return ResponseEntity.ok(this.paymentService.completePayment(paymentCompleteRequest));
    }
}