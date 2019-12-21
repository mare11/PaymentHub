package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentGatewayController implements PaymentGatewayServiceApi {

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
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        return this.paymentGatewayService.createPayment(paymentRequest);
    }

    @Override
    public PaymentResponse executePayment(PaymentResponse paymentResponse) {
        return this.paymentGatewayService.executePayment(paymentResponse);
    }
}
