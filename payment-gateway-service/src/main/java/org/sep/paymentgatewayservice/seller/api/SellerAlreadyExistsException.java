package org.sep.paymentgatewayservice.seller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SellerAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -2056469098993063091L;

    public SellerAlreadyExistsException(final String issn) {
        super("Seller with issn number " + issn + " already exists!");
    }
}