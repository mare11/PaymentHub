package org.sep.bitcoinservice.controller;

import org.sep.bitcoinservice.service.BitcoinService;
import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodResponse;
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
    public ResponseEntity<PaymentMethodResponse> createPayment(URI baseUrl, PaymentMethodRequest paymentMethodRequest) {
        return new ResponseEntity<>(bitcoinService.createOrder(paymentMethodRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> completePayment(URI baseUrl, PaymentCompleteRequest paymentCompleteRequest) {
        this.bitcoinService.completePayment(paymentCompleteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
