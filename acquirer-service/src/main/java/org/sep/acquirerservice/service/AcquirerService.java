package org.sep.acquirerservice.service;

import org.sep.acquirerservice.api.TransactionRequest;
import org.sep.acquirerservice.api.TransactionResponse;
import org.sep.acquirerservice.model.CardDto;
import org.sep.acquirerservice.model.Transaction;

public interface AcquirerService {

    TransactionResponse prepareTransaction(TransactionRequest transactionRequest);

    Transaction getTransactionById(String id);

    TransactionResponse executeTransaction(String id, CardDto cardDto);
}
