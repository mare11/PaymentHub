package org.sep.bankservice.controller;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class BankController implements PaymentMethodApi {

    @Override
    public ResponseEntity<PaymentMethodResponse> createPayment(final URI baseUrl, final PaymentMethodRequest paymentMethodRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> completePayment(final URI baseUrl, final PaymentCompleteRequest paymentCompleteRequest) {
        return null;
    }
}
