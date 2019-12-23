package org.sep.paypalservice.repository;

import org.sep.paypalservice.model.MerchantPaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPaymentDetailsRepository extends JpaRepository<org.sep.paypalservice.model.MerchantPaymentDetails, Long> {

    MerchantPaymentDetails findByMerchantId(String merchantId);
}