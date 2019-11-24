package org.sep.paymenttransactionservice.controller;

import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.sep.paymenttransactionservice.api.PaymentTransactionServiceApi;
import org.sep.paymenttransactionservice.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentTransactionController implements PaymentTransactionServiceApi {

    private final PaymentTransactionService paymentTransactionService;

    @Autowired
    public PaymentTransactionController(final PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Override
    public PaymentResponse createPayment(final PaymentRequest paymentRequest) {
        return paymentTransactionService.createPayment(paymentRequest);
    }

    @Override
    public boolean executePayment(final PaymentResponse paymentResponse) {
        return paymentTransactionService.executePayment(paymentResponse);
    }
}
