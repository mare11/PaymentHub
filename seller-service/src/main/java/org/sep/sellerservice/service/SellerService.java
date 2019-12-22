package org.sep.sellerservice.service;

import org.sep.sellerservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.dto.ChosenPaymentMethodsDto;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.PaymentMethod;
import org.sep.sellerservice.model.Seller;

import java.util.List;

public interface SellerService {

    Seller findById(Long id);

    SellerDto save(SellerRegistrationRequest sellerRegistrationRequest);

    SellerDto update(Seller seller);

    SellerDto addPaymentMethods(ChosenPaymentMethodsDto chosenPaymentMethodsDtos);

    List<PaymentMethod> getSellerPaymentMethods(Payment payment);
}
