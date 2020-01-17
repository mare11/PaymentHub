package org.sep.acquirerservice.service;

import org.sep.acquirerservice.model.Card;
import org.sep.acquirerservice.model.Transaction;
import org.sep.acquirerservice.model.TransactionRequest;
import org.sep.acquirerservice.model.TransactionResponse;

public interface AcquirerService {

    TransactionResponse prepareTransaction(TransactionRequest transactionRequest);

    Transaction getTransactionById(String id);

    TransactionResponse executeTransaction(String id, Card card);
}
