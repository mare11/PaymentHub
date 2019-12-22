package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.api.SellerPaymentMethods;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.exceptions.NoChosenPaymentMethodException;
import org.sep.sellerservice.exceptions.NoSellerFoundException;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.repository.PaymentMethodRepository;
import org.sep.sellerservice.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentGatewayServiceApi paymentGatewayServiceApi;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public SellerServiceImpl(SellerRepository sellerRepository, PaymentMethodRepository paymentMethodRepository, PaymentGatewayServiceApi paymentGatewayServiceApi) {
        this.sellerRepository = sellerRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentGatewayServiceApi = paymentGatewayServiceApi;
    }

    @Override
    public Seller findById(Long id) {
        return this.sellerRepository.findById(id).orElse(null);
    }

    @Override
    public SellerDto save(final SellerRegistrationRequest sellerRegistrationRequest) {
        if (Stream.of(sellerRegistrationRequest, sellerRegistrationRequest.getName(), sellerRegistrationRequest.getIssn())
                .anyMatch(Objects::isNull)) {
            return null;
        }
        Seller seller = Seller.builder()
                .name(sellerRegistrationRequest.getName())
                .issn(sellerRegistrationRequest.getIssn())
                .enabled(false)
                .build();
        seller = this.sellerRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public SellerDto update(Seller seller) {
        if (Stream.of(seller, seller.getId()).anyMatch(Objects::isNull)) {
            return null;
        }

        if (this.findById(seller.getId()) == null) {
            throw new NoSellerFoundException(seller.getId());
        }

        seller = this.sellerRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public SellerDto addPaymentMethods(final SellerPaymentMethods sellerPaymentMethods) {
        if (Stream.of(sellerPaymentMethods, sellerPaymentMethods.getSellerId(), sellerPaymentMethods.getPaymentMethods())
                .anyMatch(Objects::isNull)) {
            return null;
        }

        if (sellerPaymentMethods.getPaymentMethods().isEmpty()) {
            throw new NoChosenPaymentMethodException();
        }

        final Seller seller = this.findById(sellerPaymentMethods.getSellerId());

        if (seller == null) {
            throw new NoSellerFoundException(sellerPaymentMethods.getSellerId());
        }

        sellerPaymentMethods.getPaymentMethods().forEach(method ->
                seller.getPaymentMethodEntities().add(paymentMethodRepository.getOne(method.getId())));

        //call gateway service and send him selected payment methods

        seller.setEnabled(true);

        return this.update(seller);
    }

    @Override
    public List<PaymentMethod> getSellerPaymentMethods(Payment payment) {
        Assert.notNull(payment, "Payment can't be null!.");
        Assert.notNull(payment.getSeller(), "Seller for payment can't be null.");
        Seller seller = this.findById(payment.getSeller().getId());
        if (seller == null) {
            return null;
        }
        return seller.getPaymentMethodEntities().stream()
                .map(method -> modelMapper.map(method, PaymentMethod.class))
                .collect(Collectors.toList());
    }
}