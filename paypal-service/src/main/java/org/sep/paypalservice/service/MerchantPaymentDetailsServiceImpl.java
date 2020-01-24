package org.sep.paypalservice.service;

import lombok.extern.slf4j.Slf4j;
import org.sep.paypalservice.exceptions.NoMerchantFoundException;
import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.sep.paypalservice.repository.MerchantPaymentDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MerchantPaymentDetailsServiceImpl implements MerchantPaymentDetailsService {

    private final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository;

    @Autowired
    public MerchantPaymentDetailsServiceImpl(final MerchantPaymentDetailsRepository merchantPaymentDetailsRepository) {
        this.merchantPaymentDetailsRepository = merchantPaymentDetailsRepository;
    }

    @Override
    public MerchantPaymentDetails findByMerchantId(final String merchantId) {
        final MerchantPaymentDetails merchantPaymentDetails = this.merchantPaymentDetailsRepository.findByMerchantId(merchantId);
        if (merchantPaymentDetails == null) {
            log.error("Merchant with id '{}' does not exist", merchantId);
            throw new NoMerchantFoundException(merchantId);
        }
        log.info("Merchant is retrieved from DB...");
        return merchantPaymentDetails;
    }

    @Override
    public MerchantPaymentDetails save(MerchantPaymentDetails merchantPaymentDetails) {
        log.info("Saving payment details of merchant with id '{}'...", merchantPaymentDetails.getMerchantId());
        merchantPaymentDetails = this.merchantPaymentDetailsRepository.save(merchantPaymentDetails);
        log.info("Payment details for merchant with id '{} are saved into DB successfully", merchantPaymentDetails.getMerchantId());
        return merchantPaymentDetails;
    }
}