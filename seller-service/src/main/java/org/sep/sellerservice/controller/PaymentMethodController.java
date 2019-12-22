package org.sep.sellerservice.controller;

import org.sep.sellerservice.api.PaymentMethodServiceApi;
import org.sep.sellerservice.model.PaymentMethod;
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
    public PaymentMethodController(PaymentMethodService paymentDataService) {
        this.paymentDataService = paymentDataService;
    }

    @Override
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return new ResponseEntity<>(this.paymentDataService.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PaymentMethod> addPaymentMethod(final PaymentMethod paymentMethod) {
        try {
            return new ResponseEntity<>(this.paymentDataService.save(paymentMethod), HttpStatus.OK);
        } catch (DataAccessException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}