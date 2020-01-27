package org.sep.bankservice.controller;

import org.sep.bankservice.service.BankService;
import org.sep.paymentgatewayservice.method.api.PaymentMethodApi;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
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
    public BankController(final BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(final URI baseUrl, final PaymentRequest paymentRequest) {
        return ResponseEntity.ok(this.bankService.createPayment(paymentRequest));
    }

    @Override
    public ResponseEntity<String> retrieveMerchantRegistrationUrl(final URI baseUrl, final String merchantId) {
        return ResponseEntity.ok(this.bankService.retrieveMerchantRegistrationUrl(merchantId));
    }

    @Override
    public ResponseEntity<MerchantOrderStatus> getOrderStatus(URI baseUrl, String orderId) {
        return ResponseEntity.ok(this.bankService.getOrderStatus(orderId));
    }
}
