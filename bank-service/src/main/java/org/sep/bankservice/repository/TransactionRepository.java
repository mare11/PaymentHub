package org.sep.bankservice.repository;

import org.sep.bankservice.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    TransactionEntity findByOrderId(String orderId);
}
