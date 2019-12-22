package org.sep.sellerservice.service;

import org.sep.sellerservice.api.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethod> findAll();

    PaymentMethod save(PaymentMethod paymentMethod);
}
