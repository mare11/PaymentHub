package org.sep.paypalservice.repository;

import org.sep.paypalservice.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    PaymentTransaction findByOrderId(String orderId);
}