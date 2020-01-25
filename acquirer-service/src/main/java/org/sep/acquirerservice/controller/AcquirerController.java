package org.sep.acquirerservice.controller;

import org.sep.acquirerservice.model.TransactionRequest;
import org.sep.acquirerservice.model.TransactionResponse;
import org.sep.acquirerservice.service.AcquirerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcquirerController {

    private final AcquirerService acquirerService;

    @Autowired
    public AcquirerController(AcquirerService acquirerService) {
        this.acquirerService = acquirerService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResponse prepareTransaction(@RequestBody TransactionRequest transactionRequest) {
        return acquirerService.prepareTransaction(transactionRequest);
    }
}
