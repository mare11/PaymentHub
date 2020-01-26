package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.MerchantPaymentMethodsDto;
import org.sep.sellerservice.model.Merchant;

import java.util.List;

public interface MerchantService {

    Merchant findById(String id);

    Merchant save(MerchantRequest merchantRequest);

    void update(Merchant merchant);

    String addPaymentMethods(MerchantPaymentMethodsDto merchantPaymentMethodsDto);

    List<PaymentMethod> getSellerPaymentMethods(String id);

    void enableMerchant(String merchantId);
}
