package org.sep.sellerservice.repository;

import org.sep.sellerservice.model.MerchantPaymentMethod;
import org.sep.sellerservice.model.MerchantPaymentMethodId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPaymentMethodRepository extends JpaRepository<MerchantPaymentMethod, MerchantPaymentMethodId> {

    MerchantPaymentMethod findById_Merchant_IdAndId_PaymentMethodEntity_Id(String merchantId, Long methodId);
}