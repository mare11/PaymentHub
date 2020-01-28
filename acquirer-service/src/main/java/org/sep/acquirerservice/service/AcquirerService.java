package org.sep.acquirerservice.service;

import org.sep.acquirerservice.model.*;
import org.sep.pccservice.api.TransactionStatus;

public interface AcquirerService {

    TransactionResponse prepareTransaction(TransactionRequest transactionRequest);

    Transaction getTransactionById(String id);

    TransactionResponse submitTransaction(String id, Card card);

    TransactionStatus getTransactionStatus(String id);

    Boolean checkClientExistence(Client client);
}
