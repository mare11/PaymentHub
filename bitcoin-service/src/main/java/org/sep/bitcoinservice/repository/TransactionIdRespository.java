package org.sep.bitcoinservice.repository;

import org.sep.bitcoinservice.model.TransactionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionIdRespository extends JpaRepository<TransactionId, Long> {
}
