package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Transaction;
import org.sep.bitcoinservice.model.TransactionStatus;

import java.util.List;

public interface TransactionService {

    Transaction findByOrderId(Long orderId);
    List<Transaction> findByStatus(TransactionStatus transactionStatus);
    Transaction save(Transaction transaction);
}
