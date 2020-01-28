package org.sep.acquirerservice.controller;

import org.sep.acquirerservice.model.Client;
import org.sep.acquirerservice.model.TransactionRequest;
import org.sep.acquirerservice.model.TransactionResponse;
import org.sep.acquirerservice.service.AcquirerService;
import org.sep.pccservice.api.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/acquirer")
public class AcquirerController {

    private final AcquirerService acquirerService;

    @Autowired
    public AcquirerController(AcquirerService acquirerService) {
        this.acquirerService = acquirerService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionStatus> getTransactionStatus(@PathVariable String id) {
        return ResponseEntity.ok(acquirerService.getTransactionStatus(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionResponse> prepareTransaction(@RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(acquirerService.prepareTransaction(transactionRequest));
    }

    @PostMapping(value = "/client", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkClientExistence(@RequestBody Client client) {
        return ResponseEntity.ok(acquirerService.checkClientExistence(client));
    }
}
