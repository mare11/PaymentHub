package org.sep.issuerservice.repository;

import org.sep.issuerservice.model.TransactionEntity;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findAllByStatus(TransactionStatus status);

}
