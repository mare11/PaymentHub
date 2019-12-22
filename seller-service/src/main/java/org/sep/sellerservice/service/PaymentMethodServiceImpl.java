package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.model.PaymentMethodEntity;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public PaymentMethodServiceImpl(final PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        modelMapper = new ModelMapper();
    }

    @Override
    public List<PaymentMethod> findAll() {
        return this.paymentMethodRepository.findAll()
                .stream()
                .map(method -> modelMapper.map(method, PaymentMethod.class))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentMethod save(final PaymentMethod paymentMethod) {
        if (Stream.of(paymentMethod, paymentMethod.getName()).anyMatch(Objects::isNull)) {
            return null;
        }
        PaymentMethodEntity paymentMethodEntity = this.paymentMethodRepository.save(modelMapper.map(paymentMethod, PaymentMethodEntity.class));
        return modelMapper.map(paymentMethodEntity, PaymentMethod.class);
    }
}