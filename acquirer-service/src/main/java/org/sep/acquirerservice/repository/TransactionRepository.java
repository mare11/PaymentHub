package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.TransactionEntity;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByStatus(TransactionStatus status);
}
