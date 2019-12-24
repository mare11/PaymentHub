package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.model.PaymentMethodEntity;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PaymentMethodServiceImpl(final PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public List<PaymentMethod> findAll() {
        return this.paymentMethodRepository.findAll()
                .stream()
                .map(method -> this.modelMapper.map(method, PaymentMethod.class))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentMethod save(final PaymentMethod paymentMethod) {
        Assert.notNull(paymentMethod, "Payment method object can't be null!");
        Assert.notNull(paymentMethod.getName(), "Payment method name can't be null!");

        if (this.paymentMethodRepository.findByName(paymentMethod.getName()) != null) return null;

        PaymentMethodEntity paymentMethodEntity = this.paymentMethodRepository.save(this.modelMapper.map(paymentMethod, PaymentMethodEntity.class));
        return this.modelMapper.map(paymentMethodEntity, PaymentMethod.class);
    }
}