package org.sep.paymentclientservice.controller;

import org.sep.paymentclientservice.api.PaymentClientServiceApi;
import org.sep.paymentclientservice.service.PaymentClientService;
import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentClientController implements PaymentClientServiceApi {

    private final PaymentClientService paymentClientService;

    @Autowired
    public PaymentClientController(final PaymentClientService paymentClientService) {
        this.paymentClientService = paymentClientService;
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) {
        return paymentClientService.createPayment(paymentRequest);
    }

    @Override
    public boolean executePayment(final PaymentResponse paymentResponse) {
        return paymentClientService.executePayment(paymentResponse);
    }
}
