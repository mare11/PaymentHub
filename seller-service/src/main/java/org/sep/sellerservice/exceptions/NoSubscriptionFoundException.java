package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSubscriptionFoundException extends RuntimeException {

    private static final long serialVersionUID = 1908650518662786704L;

    public NoSubscriptionFoundException(final Long id) {
        super("Subscription with id " + id + " is not found.");
    }

    public NoSubscriptionFoundException(final String subscriptionId) {
        super("Subscription with subscription id " + subscriptionId + " is not found.");
    }
}