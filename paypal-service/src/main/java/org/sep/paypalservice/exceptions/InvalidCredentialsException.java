package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = -2758420770393335201L;

    public InvalidCredentialsException() {
        super("Invalid client id and/or client secret!");
    }
}