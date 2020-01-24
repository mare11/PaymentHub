package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSubscriptionFoundException extends RuntimeException {

    private static final long serialVersionUID = -7105952489881644831L;

    public NoSubscriptionFoundException(final String id) {
        super("Subscription with id " + id + " is not found.");
    }
}