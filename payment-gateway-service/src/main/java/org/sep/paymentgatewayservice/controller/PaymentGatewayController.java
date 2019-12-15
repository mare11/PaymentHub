package org.sep.paymentgatewayservice.controller;

import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.PaymentRequest;
import org.sep.paymentgatewayservice.api.PaymentResponse;
import org.sep.paymentgatewayservice.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentGatewayController implements PaymentGatewayServiceApi {

    private PaymentGatewayService paymentGatewayService;

    @Autowired
    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @Override
    public PaymentResponse preparePayment(PaymentRequest paymentRequest) {
        return paymentGatewayService.preparePayment(paymentRequest);
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        return paymentGatewayService.createPayment(paymentRequest);
    }

    @Override
    public PaymentResponse executePayment(PaymentResponse paymentResponse) {
        return paymentGatewayService.executePayment(paymentResponse);
    }
}
