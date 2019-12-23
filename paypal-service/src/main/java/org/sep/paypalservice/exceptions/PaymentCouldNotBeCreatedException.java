package org.sep.paypalservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentCouldNotBeCreatedException extends RuntimeException {

    private static final long serialVersionUID = 5575933812190036094L;

    public PaymentCouldNotBeCreatedException(String message) {
        super("Requested payment could not be created. Reason: " + message);
    }
}