package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    CardEntity findByPanAndCcv(String pan, String ccv);

    CardEntity findByPanAndCcvAndExpirationDate(String pan, String ccv, LocalDate expirationDate);

    CardEntity findByMerchantIdAndMerchantPassword(String merchantId, String merchantPassword);

}
