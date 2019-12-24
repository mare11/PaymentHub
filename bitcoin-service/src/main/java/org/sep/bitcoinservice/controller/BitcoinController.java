package org.sep.bitcoinservice.controller;

import org.sep.bitcoinservice.service.BitcoinService;
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class BitcoinController implements PaymentMethodApi {

    private BitcoinService bitcoinService;

    @Autowired
    public BitcoinController(BitcoinService bitcoinService){
        this.bitcoinService = bitcoinService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(URI baseUrl, PaymentRequest paymentRequest) {
        return new ResponseEntity<>(bitcoinService.createOrder(paymentRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> completePayment(PaymentCompleteRequest paymentCompleteRequest) {
        this.bitcoinService.completePayment(paymentCompleteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
