package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.ChosenPaymentMethodsDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.exceptions.NoChosenPaymentMethodException;
import org.sep.sellerservice.exceptions.NoSellerFoundException;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.repository.SellerRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

@Service
public class SellerServiceImpl implements SellerService {

    private final SellerRegistrationRepository sellerRegistrationRepository;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public SellerServiceImpl(SellerRegistrationRepository sellerRegistrationRepository) {
        this.sellerRegistrationRepository = sellerRegistrationRepository;
    }

    @Override
    public Seller findById(Long id) {
        return this.sellerRegistrationRepository.findById(id).orElse(null);
    }

    @Override
    public SellerDto save(SellerRegistrationRequest sellerRegistrationRequest) throws DataAccessException {
        if (Stream.of(sellerRegistrationRequest, sellerRegistrationRequest.getName(), sellerRegistrationRequest.getIssn())
                .anyMatch(Objects::isNull)) {
            return null;
        }
        Seller seller = Seller.builder()
                .name(sellerRegistrationRequest.getName())
                .issn(sellerRegistrationRequest.getIssn())
                .enabled(false)
                .build();
        seller = this.sellerRegistrationRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public SellerDto update(Seller seller) throws NoSellerFoundException {
        if (Stream.of(seller, seller.getId()).anyMatch(Objects::isNull)) {
            return null;
        }

        if (this.findById(seller.getId()) == null) {
            throw new NoSellerFoundException(seller.getId());
        }

        seller = this.sellerRegistrationRepository.save(seller);
        return this.modelMapper.map(seller, SellerDto.class);
    }

    @Override
    public SellerDto addPaymentMethods(ChosenPaymentMethodsDto chosenPaymentMethodsDto) throws NoSellerFoundException, NoChosenPaymentMethodException {
        if (Stream.of(chosenPaymentMethodsDto, chosenPaymentMethodsDto.getSellerId(), chosenPaymentMethodsDto.getPaymentMethods())
                .anyMatch(Objects::isNull)) {
            return null;
        }

        if (chosenPaymentMethodsDto.getPaymentMethods().isEmpty()) {
            throw new NoChosenPaymentMethodException();
        }

        Seller seller = this.findById(chosenPaymentMethodsDto.getSellerId());

        if (seller == null) {
            throw new NoSellerFoundException(chosenPaymentMethodsDto.getSellerId());
        }

        chosenPaymentMethodsDto.getPaymentMethods().forEach(method -> {
            seller.getPaymentMethods().add(method);
        });

        seller.setEnabled(true);

        return this.update(seller);
    }
}