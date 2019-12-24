package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.methodapi.PaymentCompleteRequest;
import org.sep.paymentgatewayservice.methodapi.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.sep.paypalservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class PaymentController implements PaymentMethodApi {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(URI baseUrl, PaymentRequest paymentRequest) {
        PaymentResponse paymentMethodResponse = this.paymentService.createPayment(paymentRequest);
        return ResponseEntity.ok(paymentMethodResponse);
    }

    @Override
    public ResponseEntity<String> completePayment(PaymentCompleteRequest paymentCompleteRequest) {
        return null;
    }

    @Override
    public ResponseEntity<String> retrieveSellerRegistrationUrl(URI baseUrl, String merchantId) {
        return ResponseEntity.ok(this.paymentService.retrieveSellerRegistrationUrl(merchantId));
    }

    @PostMapping(value = "/register_seller", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerSeller(@RequestBody MerchantPaymentDetails merchantPaymentDetails) {
        return ResponseEntity.ok(this.paymentService.registerSeller(merchantPaymentDetails));
    }
}