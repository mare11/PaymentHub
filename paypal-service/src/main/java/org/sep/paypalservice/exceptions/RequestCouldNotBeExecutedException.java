package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RequestCouldNotBeExecutedException extends RuntimeException {

    private static final long serialVersionUID = 5575933812190036094L;

    public RequestCouldNotBeExecutedException(final String message) {
        super("Request to paypal sandbox api could not be proceeded. Reason: " + message);
    }
}