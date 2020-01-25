package org.sep.sellerservice.controller;

import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.paymentgatewayservice.seller.api.PaymentMethodServiceApi;
import org.sep.sellerservice.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment_method")
public class PaymentMethodController implements PaymentMethodServiceApi {

    private final PaymentMethodService paymentDataService;

    @Autowired
    public PaymentMethodController(final PaymentMethodService paymentDataService) {
        this.paymentDataService = paymentDataService;
    }

    @Override
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return new ResponseEntity<>(this.paymentDataService.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> addPaymentMethod(final PaymentMethod paymentMethod) {
        try {
            this.paymentDataService.save(paymentMethod);
            return ResponseEntity.ok().build();
        } catch (final DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}