package org.sep.scholar.controller;

import org.sep.paymentclientservice.api.PaymentClientServiceApi;
import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "scholar")
public class ScholarController {

    private final PaymentClientServiceApi paymentClientServiceApi;

    @Autowired
    public ScholarController(final PaymentClientServiceApi paymentClientServiceApi) {
        this.paymentClientServiceApi = paymentClientServiceApi;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentClientServiceApi.createPayment(paymentRequest);
    }

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean executePayment(@RequestBody PaymentResponse paymentResponse) {
        return paymentClientServiceApi.executePayment(paymentResponse);
    }
}
