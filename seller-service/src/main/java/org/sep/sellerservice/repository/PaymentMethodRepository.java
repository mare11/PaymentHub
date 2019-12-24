package org.sep.sellerservice.repository;

import org.sep.sellerservice.model.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {

    PaymentMethodEntity findByName(String name);
}