package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SellerIsNotEnabledException extends RuntimeException {

    private static final long serialVersionUID = -2056469098993063091L;

    public SellerIsNotEnabledException(String issn) {
        super("Seller with issn number " + issn + " is not enabled yet!");
    }
}