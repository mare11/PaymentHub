package org.sep.bankservice.repository;

import org.sep.bankservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByOrderId(String orderId);
}
