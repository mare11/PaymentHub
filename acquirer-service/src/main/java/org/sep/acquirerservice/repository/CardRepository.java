package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Card findByPanAndCcv(String pan, String ccv);
}
