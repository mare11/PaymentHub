package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MerchantIsAlreadyEnabledException extends RuntimeException {

    private static final long serialVersionUID = -1682311238692368353L;

    public MerchantIsAlreadyEnabledException(final String merchantId) {
        super("Merchant with merchant id " + merchantId + " is already enabled!");
    }
}