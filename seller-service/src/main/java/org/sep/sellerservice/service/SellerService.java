package org.sep.sellerservice.service;

import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.ChosenPaymentMethodsDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.model.Seller;

public interface SellerService {

    Seller findById(Long id);

    SellerDto save(SellerRegistrationRequest sellerRegistrationRequest);

    SellerDto update(Seller seller);

    SellerDto addPaymentMethods(ChosenPaymentMethodsDto chosenPaymentMethodsDtos);
}
