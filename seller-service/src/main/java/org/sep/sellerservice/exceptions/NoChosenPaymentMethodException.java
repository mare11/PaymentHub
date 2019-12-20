package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoChosenPaymentMethodException extends RuntimeException {

    private static final long serialVersionUID = 683186192637263993L;

    public NoChosenPaymentMethodException() {
        super("You have to choose at least one payment method.");
    }
}