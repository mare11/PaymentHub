package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoOrderFoundException extends RuntimeException {

    private static final long serialVersionUID = -4174544787818847222L;

    public NoOrderFoundException(String id) {
        super("Order with id " + id + " is not found.");
    }
}