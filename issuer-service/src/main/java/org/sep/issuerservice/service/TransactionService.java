package org.sep.issuerservice.service;

import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.sep.pccservice.api.TransactionStatus;

public interface TransactionService {

    TransactionStatus getTransactionStatus(String acquirerOrderId);

    PccResponse handleRequest(PccRequest request);

}
