package org.sep.paypal.controller;

import lombok.extern.slf4j.Slf4j;
import org.sep.paymenttransactionservice.api.PaymentMethodApi;
import org.sep.paymenttransactionservice.api.PaymentRequest;
import org.sep.paymenttransactionservice.api.PaymentResponse;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
public class PayPalController implements PaymentMethodApi {
    @Override
    public PaymentResponse createPayment(final URI baseUrl, final PaymentRequest paymentRequest) {
        log.info("Create payment with PayPal");
        return null;
    }

    @Override
    public boolean executePayment(final URI baseUrl, final PaymentResponse paymentResponse) {
        log.info("Execute payment with PayPal");
        return false;
    }
}
