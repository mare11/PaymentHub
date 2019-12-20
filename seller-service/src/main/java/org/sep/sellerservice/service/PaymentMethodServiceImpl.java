package org.sep.sellerservice.service;

import org.sep.sellerservice.model.PaymentMethod;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Autowired
    public PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public List<PaymentMethod> findAll() {
        return this.paymentMethodRepository.findAll();
    }

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) throws DataAccessException {
        if (Stream.of(paymentMethod, paymentMethod.getName()).anyMatch(Objects::isNull)) {
            return null;
        }
        return this.paymentMethodRepository.save(paymentMethod);
    }
}