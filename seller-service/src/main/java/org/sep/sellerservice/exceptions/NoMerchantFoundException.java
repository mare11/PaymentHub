package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoMerchantFoundException extends RuntimeException {

    private static final long serialVersionUID = 6768654592606974238L;

    public NoMerchantFoundException(final String merchantId) {
        super("Merchant with merchant id " + merchantId + " is not found.");
    }
}