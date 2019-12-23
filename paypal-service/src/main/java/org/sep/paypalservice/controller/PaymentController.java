package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodResponse;
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
    public ResponseEntity<PaymentMethodResponse> createPayment(URI baseUrl, PaymentMethodRequest paymentMethodRequest) {
        PaymentMethodResponse paymentMethodResponse = this.paymentService.createPayment(paymentMethodRequest);
        return ResponseEntity.ok(paymentMethodResponse);
    }

    @Override
    public ResponseEntity<Void> completePayment(URI baseUrl, PaymentCompleteRequest paymentCompleteRequest) {
        this.paymentService.completePayment(paymentCompleteRequest);
        return ResponseEntity.ok().build();
    }
}