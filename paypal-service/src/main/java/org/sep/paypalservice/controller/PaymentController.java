package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.dto.RegistrationDto;
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
    public PaymentController(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(final URI baseUrl, final PaymentRequest paymentRequest) {
        final PaymentResponse paymentMethodResponse = this.paymentService.createPayment(paymentRequest);
        return ResponseEntity.ok(paymentMethodResponse);
    }

    @Override
    public ResponseEntity<String> retrieveSellerRegistrationUrl(final URI baseUrl, final String merchantId) {
        return ResponseEntity.ok(this.paymentService.retrieveSellerRegistrationUrl(merchantId));
    }

    @PostMapping(value = "/register_seller", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionDto> registerSeller(@RequestBody final RegistrationDto registrationDto) {
        return ResponseEntity.ok(RedirectionDto.builder().redirectionUrl(this.paymentService.registerSeller(registrationDto)).build());
    }

    @PostMapping(value = "/payment_transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RedirectionDto> completePaymentTransaction(@RequestBody final CompleteDto completeDto) {
        return ResponseEntity.ok(this.paymentService.completePaymentTransaction(completeDto));
    }
}