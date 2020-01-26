package org.sep.bitcoinservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoTransactionFoundException extends RuntimeException {

    public NoTransactionFoundException(String orderId){
        super("Transaction with order id" + orderId +  " is not found.");
    }
}
