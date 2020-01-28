package org.sep.bankservice.repository;

import org.sep.bankservice.model.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {

    MerchantEntity findByMerchantId(String merchantId);

    MerchantEntity findByBankMerchantId(String bankMerchantId);
}
