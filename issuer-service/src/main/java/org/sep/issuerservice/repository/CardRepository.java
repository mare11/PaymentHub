package org.sep.issuerservice.repository;

import org.sep.issuerservice.model.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    CardEntity findByPanAndCcv(String pan, String ccv);
}
