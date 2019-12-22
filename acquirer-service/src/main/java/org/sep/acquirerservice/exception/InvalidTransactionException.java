package org.sep.acquirerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends RuntimeException {

    private static final long serialVersionUID = -1508537151382963883L;

    public InvalidTransactionException() {
        super("Invalid transaction data!");
    }
}
