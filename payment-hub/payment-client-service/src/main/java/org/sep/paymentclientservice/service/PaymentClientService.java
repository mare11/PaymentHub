package org.sep.paymentclientservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.sep.paymenttransactionservice.api.PaymentTransactionServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentClientService {

    private final PaymentTransactionServiceApi paymentTransactionServiceApi;

    @Autowired
    public PaymentClientService(final PaymentTransactionServiceApi paymentTransactionServiceApi) {
        this.paymentTransactionServiceApi = paymentTransactionServiceApi;
    }

    public PaymentResponse createPayment(final PaymentRequest paymentRequest) {
        return paymentTransactionServiceApi.createPayment(paymentRequest);
    }

    public boolean executePayment(final PaymentResponse paymentResponse) {
        return paymentTransactionServiceApi.executePayment(paymentResponse);
    }
}
