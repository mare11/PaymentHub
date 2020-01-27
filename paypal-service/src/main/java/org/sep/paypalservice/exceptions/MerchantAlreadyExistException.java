package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MerchantAlreadyExistException extends RuntimeException {

    private static final long serialVersionUID = -6146090235293771571L;

    public MerchantAlreadyExistException(final String id) {
        super("Merchant with id " + id + " already exist.");
    }
}