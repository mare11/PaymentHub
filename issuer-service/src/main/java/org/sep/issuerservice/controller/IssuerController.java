package org.sep.issuerservice.controller;

import org.sep.issuerservice.service.TransactionService;
import org.sep.pccservice.api.PccRequest;
import org.sep.pccservice.api.PccResponse;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IssuerController {

    private final TransactionService transactionService;

    @Autowired
    public IssuerController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(value = "/{acquirerOrderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionStatus> getTransactionStatus(@PathVariable String acquirerOrderId) {
        return ResponseEntity.ok(transactionService.getTransactionStatus(acquirerOrderId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PccResponse> handle(@RequestBody PccRequest request) {
        return ResponseEntity.ok(transactionService.handleRequest(request));
    }
}
