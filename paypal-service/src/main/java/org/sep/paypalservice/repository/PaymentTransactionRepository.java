package org.sep.paypalservice.repository;

import org.sep.paypalservice.model.PaymentTransaction;
import org.sep.paypalservice.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    PaymentTransaction findByOrderId(String orderId);

    List<PaymentTransaction> findAllByStatus(TransactionStatus transactionStatus);
}