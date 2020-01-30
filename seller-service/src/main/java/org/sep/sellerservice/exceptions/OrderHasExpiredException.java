package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OrderHasExpiredException extends RuntimeException {

    private static final long serialVersionUID = 2906626455378692818L;

    public OrderHasExpiredException(final String orderId) {
        super("Order with id " + orderId + " has expired.");
    }
}