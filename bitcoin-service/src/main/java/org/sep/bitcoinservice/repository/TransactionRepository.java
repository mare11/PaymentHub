package org.sep.bitcoinservice.repository;

import org.sep.bitcoinservice.model.Transaction;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction findByOrderId(String orderId);

    List<Transaction> findAllByStatus(MerchantOrderStatus transactionStatus);
}
