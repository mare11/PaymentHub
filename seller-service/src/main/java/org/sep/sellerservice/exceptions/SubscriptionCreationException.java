package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SubscriptionCreationException extends RuntimeException {

    private static final long serialVersionUID = -8575386178304987274L;

    public SubscriptionCreationException(final String planId) {
        super("Creating subscription on plan with id " + planId + " has failed! Please, try again later.");
    }
}