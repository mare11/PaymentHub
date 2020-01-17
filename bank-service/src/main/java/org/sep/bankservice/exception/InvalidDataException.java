package org.sep.bankservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = -4379122660382757934L;

    public InvalidDataException() {
        super("Invalid payment data!");
    }
}
