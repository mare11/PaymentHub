package org.sep.paymentdataservice.controller;

import org.sep.paymentdataservice.api.PaymentDataServiceApi;
import org.sep.paymentdataservice.api.PaymentMethod;
import org.sep.paymentdataservice.service.PaymentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class PaymentDataController implements PaymentDataServiceApi {

    private final PaymentDataService paymentDataService;

    @Autowired
    public PaymentDataController(final PaymentDataService paymentDataService) {
        this.paymentDataService = paymentDataService;
    }

    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentDataService.getAllPaymentMethods();
    }

    @Override
    public Optional<PaymentMethod> getPaymentMethodByName(final String name) {
        return paymentDataService.getPaymentMethodByName(name);
    }

    @Override
    public void addPaymentMethod(final PaymentMethod paymentMethod) {
        paymentDataService.addPaymentMethod(paymentMethod);
    }
}
