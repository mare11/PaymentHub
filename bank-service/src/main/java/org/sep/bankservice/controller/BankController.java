package org.sep.bankservice.controller;

import org.sep.bankservice.service.BankService;
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class BankController implements PaymentMethodApi {

    private final BankService bankService;

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(URI baseUrl, PaymentRequest paymentRequest) {
        return ResponseEntity.ok(bankService.createPayment(paymentRequest));
    }

    @Override
    public ResponseEntity<String> completePayment(PaymentCompleteRequest paymentCompleteRequest) {
        return null;
    }

    @Override
    public ResponseEntity<String> retrieveSellerRegistrationUrl(URI baseUrl, String merchantId) {
        return ResponseEntity.ok(bankService.retrieveSellerRegistrationUrl(merchantId));
    }
}
