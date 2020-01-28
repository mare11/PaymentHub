package org.sep.bankservice.repository;

import org.sep.bankservice.model.TransactionEntity;
import org.sep.paymentgatewayservice.payment.entity.MerchantOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    TransactionEntity findByOrderId(String orderId);

    List<TransactionEntity> findAllByStatus(MerchantOrderStatus status);
}
