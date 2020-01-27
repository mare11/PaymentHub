package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MerchantIsNotEnabledException extends RuntimeException {

    private static final long serialVersionUID = -2056469098993063091L;

    public MerchantIsNotEnabledException(final String merchantId) {
        super("Merchant with merchant id " + merchantId + " is not enabled yet!");
    }
}