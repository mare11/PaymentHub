package org.sep.sellerservice.service;

import org.modelmapper.ModelMapper;
import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.model.Seller;
import org.sep.sellerservice.repository.SellerRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SellerRegistrationServiceImpl implements SellerRegistrationService {

    private SellerRegistrationRepository sellerRegistrationRepository;
    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public SellerRegistrationServiceImpl(SellerRegistrationRepository sellerRegistrationRepository) {
        this.sellerRegistrationRepository = sellerRegistrationRepository;
    }

    @Override
    public SellerDto save(SellerRegistrationRequest sellerRegistrationRequest) {
        Seller seller = Seller.builder()
                .name(sellerRegistrationRequest.getName())
                .issn(sellerRegistrationRequest.getIssn())
                .enabled(false)
                .build();
        seller = this.sellerRegistrationRepository.save(seller);
        return modelMapper.map(seller, SellerDto.class);
    }
}