package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoPlanFoundException extends RuntimeException {

    private static final long serialVersionUID = 9012125350596220813L;

    public NoPlanFoundException(final Long id) {
        super("Plan with id " + id + " is not found.");
    }
}