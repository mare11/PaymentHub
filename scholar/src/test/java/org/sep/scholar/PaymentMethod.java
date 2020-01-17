package org.sep.scholar;

public enum PaymentMethod {
    PAYPAL(" PayPal "),
    BITCOIN(" Bitcoin "),
    BANK(" Bank ");

    private final String name;

    PaymentMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}