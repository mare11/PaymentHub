package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSubscriptionPlansFoundException extends RuntimeException {

    private static final long serialVersionUID = -5403844241256352782L;

    public NoSubscriptionPlansFoundException(final String merchantId) {
        super("Subscription plans for merchant with id " + merchantId + " are not found.");
    }
}