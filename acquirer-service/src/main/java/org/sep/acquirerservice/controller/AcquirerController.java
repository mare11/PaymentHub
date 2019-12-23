package org.sep.acquirerservice.controller;

import org.sep.acquirerservice.api.AcquirerServiceApi;
import org.sep.acquirerservice.api.TransactionRequest;
import org.sep.acquirerservice.api.TransactionResponse;
import org.sep.acquirerservice.service.AcquirerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcquirerController implements AcquirerServiceApi {

    private final AcquirerService acquirerService;

    @Autowired
    public AcquirerController(AcquirerService acquirerService) {
        this.acquirerService = acquirerService;
    }

    @Override
    @PostMapping(value = "/prepare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResponse prepareTransaction(@RequestBody TransactionRequest transactionRequest) {
        return acquirerService.prepareTransaction(transactionRequest);
    }
}
