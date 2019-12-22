package org.sep.acquirerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransactionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9004130632918251514L;

    public TransactionNotFoundException(String id) {
        super("Transaction with id: '" + id + "' not found !");
    }

}
