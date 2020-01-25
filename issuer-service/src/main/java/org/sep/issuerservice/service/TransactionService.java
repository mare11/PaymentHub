package org.sep.issuerservice.service;

import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;

public interface TransactionService {

    PccResponse handleRequest(PccRequest request);

}
