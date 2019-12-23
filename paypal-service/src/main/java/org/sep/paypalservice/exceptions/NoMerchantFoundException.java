package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoMerchantFoundException extends RuntimeException {

    private static final long serialVersionUID = -1391562833799770430L;

    public NoMerchantFoundException(String id) {
        super("Merchant with id " + id + " is not found.");
    }
}