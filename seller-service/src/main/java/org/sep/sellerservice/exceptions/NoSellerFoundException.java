package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSellerFoundException extends RuntimeException {

    private static final long serialVersionUID = 6768654592606974238L;

    public NoSellerFoundException(Long id) {
        super("Seller with id " + id + " is not found.");
    }
}