package org.sep.pccservice.service;

import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;

public interface TransactionService {

    PccResponse forwardRequest(PccRequest request);
}
