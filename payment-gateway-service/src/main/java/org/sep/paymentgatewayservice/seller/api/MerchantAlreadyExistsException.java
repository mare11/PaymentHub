package org.sep.paymentgatewayservice.seller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MerchantAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -2056469098993063091L;

    public MerchantAlreadyExistsException(final String merchantId) {
        super("Merchant with merchant id " + merchantId + " already exists!");
    }
}