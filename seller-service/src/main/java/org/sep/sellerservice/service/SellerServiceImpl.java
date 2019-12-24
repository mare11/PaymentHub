package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.paymentgatewayservice.api.PaymentGatewayServiceApi;
import org.sep.paymentgatewayservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.api.SellerAlreadyExistsException;
import org.sep.sellerservice.api.SellerPaymentMethods;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.dto.SellerPaymentMethodsDto;
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
    public SellerDto save(final SellerRegistrationRequest sellerRegistrationRequest) throws SellerAlreadyExistsException {
        Assert.notNull(sellerRegistrationRequest, "Seller registration object can't be null!");
        Assert.noNullElements(
                Stream.of(sellerRegistrationRequest.getName(),
                        sellerRegistrationRequest.getIssn(),
                        sellerRegistrationRequest.getReturnUrl())
                        .toArray(),
                "One or more fields are not specified.");

        if (this.sellerRepository.findByIssn(sellerRegistrationRequest.getIssn()) != null)
            throw new SellerAlreadyExistsException(sellerRegistrationRequest.getIssn());

        Seller seller = Seller.builder()
                .name(sellerRegistrationRequest.getName())
                .issn(sellerRegistrationRequest.getIssn())
                .returnUrl(sellerRegistrationRequest.getReturnUrl())
                .enabled(false)
                .build();
        seller = this.sellerRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public SellerDto update(Seller seller) throws NoSellerFoundException {
        Assert.notNull(seller, "Seller object can't be null!");
        Assert.notNull(seller.getId(), "Seller id can't be null!");

        if (this.findById(seller.getId()) == null) {
            throw new NoSellerFoundException(seller.getId());
        }

        seller = this.sellerRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public String addPaymentMethods(final SellerPaymentMethodsDto sellerPaymentMethodsDto) throws NoSellerFoundException {
        Assert.notNull(sellerPaymentMethodsDto, "Seller payment methods object can't be null!");
        Assert.noNullElements(
                Stream.of(sellerPaymentMethodsDto.getSellerId(),
                        sellerPaymentMethodsDto.getPaymentMethods())
                        .toArray(),
                "One or more fields are not specified.");

        if (sellerPaymentMethodsDto.getPaymentMethods().isEmpty()) {
            throw new NoChosenPaymentMethodException();
        }

        final Seller seller = this.sellerRepository.findById(sellerPaymentMethodsDto.getSellerId()).orElse(null);

        if (seller == null) {
            throw new NoSellerFoundException(sellerPaymentMethodsDto.getSellerId());
        }

        SellerPaymentMethods sellerPaymentMethods = SellerPaymentMethods.builder()
                .sellerIssn(seller.getIssn())
                .returnUrl(seller.getReturnUrl())
                .paymentMethods(sellerPaymentMethodsDto.getPaymentMethods())
                .build();

        sellerPaymentMethods.getPaymentMethods().forEach(method ->
                seller.getPaymentMethodEntities().add(this.paymentMethodRepository.getOne(method.getId())));

        this.update(seller);

        return this.paymentGatewayServiceApi.registerPaymentMethods(sellerPaymentMethods).getBody();
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
                .map(method -> this.modelMapper.map(method, PaymentMethod.class))
                .collect(Collectors.toList());
    }

    @Override
    public void enableSeller(String sellerIssn) {
        Assert.notNull(sellerIssn, "Seller issn can't be null.");

        final Seller seller = this.sellerRepository.findByIssn(sellerIssn);

        if (seller == null) {
            throw new NoSellerFoundException(sellerIssn);
        }

        seller.setEnabled(true);

        this.update(seller);
    }
}