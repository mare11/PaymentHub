package org.sep.bitcoinservice.service;

import org.sep.bitcoinservice.model.Transaction;

public interface TransactionService {

    Transaction findByOrderId(Long orderId);
    Transaction save(Transaction transaction);
}
