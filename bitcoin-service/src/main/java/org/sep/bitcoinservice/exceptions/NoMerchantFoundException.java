package org.sep.bitcoinservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoMerchantFoundException extends RuntimeException {

    public NoMerchantFoundException(String issn) {
        super("Merchant with issn number " + issn + " is not found.");
    }
}