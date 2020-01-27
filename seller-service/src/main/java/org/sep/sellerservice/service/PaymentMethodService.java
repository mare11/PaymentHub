package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.model.PaymentMethodEntity;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethod> findAll();

    PaymentMethodEntity findByName(String name);

    PaymentMethod save(PaymentMethod paymentMethod);
}
