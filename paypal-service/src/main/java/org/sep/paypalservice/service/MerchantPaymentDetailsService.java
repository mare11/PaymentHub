package org.sep.paypalservice.service;

import org.sep.paypalservice.model.MerchantPaymentDetails;

public interface MerchantPaymentDetailsService {

    MerchantPaymentDetails findByMerchantId(final String merchantId);

    MerchantPaymentDetails save(MerchantPaymentDetails merchantPaymentDetails);
}