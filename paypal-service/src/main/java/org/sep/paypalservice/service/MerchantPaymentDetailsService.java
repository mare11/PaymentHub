package org.sep.paypalservice.service;

import org.sep.paypalservice.dto.CompleteDto;
import org.sep.paypalservice.dto.RegistrationDto;
import org.sep.paypalservice.model.MerchantPaymentDetails;

public interface MerchantPaymentDetailsService {

    MerchantPaymentDetails findByMerchantId(final String merchantId);

    void save(MerchantPaymentDetails merchantPaymentDetails);

    String retrieveMerchantRegistrationUrl(String merchantId);

    CompleteDto registerMerchant(RegistrationDto registrationDto);
}