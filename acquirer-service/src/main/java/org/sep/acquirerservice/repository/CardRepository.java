package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Card findByPanAndCcv(String pan, String ccv);

    Card findByPanAndCcvAndExpirationDate(String pan, String ccv, LocalDate expirationDate);
}
