package org.sep.paypalservice.controller;

import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.PaymentRequest;
import org.sep.paymentgatewayservice.payment.entity.PaymentResponse;
import org.sep.paypalservice.dto.RedirectionDto;
import org.sep.paypalservice.dto.RegistrationDto;
import org.sep.paypalservice.model.PaymentTransaction;
import org.sep.paypalservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/payment_transaction/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentTransaction> getPaymentTransaction(@PathVariable final String orderId) {
        return ResponseEntity.ok(this.paymentService.findByOrderId(orderId));
    }
}