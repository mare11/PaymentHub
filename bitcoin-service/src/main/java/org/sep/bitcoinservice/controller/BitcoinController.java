package org.sep.bitcoinservice.controller;

import org.sep.bitcoinservice.model.Merchant;
import org.sep.bitcoinservice.service.BitcoinService;
import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class BitcoinController implements PaymentMethodApi {

    private final BitcoinService bitcoinService;

    @Autowired
    public BitcoinController(final BitcoinService bitcoinService) {
        this.bitcoinService = bitcoinService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(final URI baseUrl, final PaymentRequest paymentRequest) {
        return new ResponseEntity<>(this.bitcoinService.createOrder(paymentRequest), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> retrieveSellerRegistrationUrl(final URI baseUrl, final String merchantId) {
        return new ResponseEntity<>(this.bitcoinService.retrieveSellerRegistrationUrl(merchantId), HttpStatus.OK);
    }

    @PostMapping(value = "/register_seller", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerSeller(@RequestBody final Merchant merchant) {
        return ResponseEntity.ok(this.bitcoinService.registerSeller(merchant));
    }
}
