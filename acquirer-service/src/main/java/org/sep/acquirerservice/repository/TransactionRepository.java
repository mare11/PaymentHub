package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.TransactionEntity;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findAllByStatus(TransactionStatus status);
}
