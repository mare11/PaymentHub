package org.sep.sellerservice.service;

import org.sep.paymentgatewayservice.api.MerchantRequest;
import org.sep.paymentgatewayservice.payment.entity.NotifyPaymentMethodRegistrationDto;
import org.sep.paymentgatewayservice.seller.api.PaymentMethod;
import org.sep.sellerservice.dto.MerchantPaymentMethodsDto;
import org.sep.sellerservice.dto.MerchantRegistrationResponse;
import org.sep.sellerservice.dto.PaymentMethodDto;
import org.sep.sellerservice.model.Merchant;

import java.util.List;

public interface MerchantService {

    Merchant findById(String id);

    Merchant save(MerchantRequest merchantRequest);

    void update(Merchant merchant);

    MerchantRegistrationResponse addPaymentMethods(MerchantPaymentMethodsDto merchantPaymentMethodsDto);

    List<PaymentMethodDto> getMerchantPaymentMethodsRegistrationUrls(String id);

    List<PaymentMethod> getMerchantPaymentMethods(String id);

    MerchantRegistrationResponse confirmPaymentMethodsRegistration(String id);

    Boolean notifyMerchantIsRegistered(final NotifyPaymentMethodRegistrationDto notifyPaymentMethodRegistrationDto);
}
