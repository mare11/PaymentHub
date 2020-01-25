package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.seller.api.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethod> findAll();

    PaymentMethod save(PaymentMethod paymentMethod);
}
