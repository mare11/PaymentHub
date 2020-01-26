package org.sep.sellerservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoPaymentFoundException extends RuntimeException {

    private static final long serialVersionUID = 6768654592606974238L;

    public NoPaymentFoundException(final String orderId) {
        super("Payment with order id " + orderId + " is not found.");
    }
}