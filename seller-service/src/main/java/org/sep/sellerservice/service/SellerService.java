package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.SellerRegistrationRequest;
import org.sep.sellerservice.api.PaymentMethod;
import org.sep.sellerservice.dto.SellerDto;
import org.sep.sellerservice.dto.SellerPaymentMethodsDto;
import org.sep.sellerservice.model.Payment;
import org.sep.sellerservice.model.Seller;

import java.util.List;

public interface SellerService {

    Seller findById(Long id);

    SellerDto save(SellerRegistrationRequest sellerRegistrationRequest);

    SellerDto update(Seller seller);

    String addPaymentMethods(SellerPaymentMethodsDto sellerPaymentMethodsDto);

    List<PaymentMethod> getSellerPaymentMethods(Payment payment);

    void enableSeller(String sellerIssn);
}
