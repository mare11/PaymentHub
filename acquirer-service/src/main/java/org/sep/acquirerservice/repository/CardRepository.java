package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    CardEntity findByPan(String pan);

    CardEntity findByPanAndCcv(String pan, String ccv);

    CardEntity findByMerchantIdAndMerchantPassword(String merchantId, String merchantPassword);

}
