package org.sep.paymentgatewayservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoPaymentMethodFoundException extends RuntimeException {

    private static final long serialVersionUID = 8472912681289139465L;

    public NoPaymentMethodFoundException(String method) {
        super("Payment method with name '" + method + "' is not found!");
    }
}