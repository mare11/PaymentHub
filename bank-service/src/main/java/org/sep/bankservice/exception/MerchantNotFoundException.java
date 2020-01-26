package org.sep.bankservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MerchantNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -5222168666948269979L;

    public MerchantNotFoundException(String merchantId) {
        super("Merchant with id: " + merchantId + " not found!");
    }
}
