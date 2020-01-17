package org.sep.scholar;

public class Data {

    static final String HTTP_PREFIX = "http://";
    static final String HOSTNAME = "localhost";
    static final String SCIENTIFIC_CENTER_SERVER_PORT = "9000";
    static final String SCIENTIFIC_CENTER_PAYMENT_PATH = "/payment";
    static final String SELLER_TITLE = "Seller Service";
    static final String PAYMENT_COMPLETED = "Payment completed";
    static final String SCIENTIFIC_CENTER_HOMEPAGE = "Homepage";

    static final String PAYPAL_SANDBOX_LOGIN_TITLE = "Log in to your PayPal account";
    static final String PAYPAL_SANDBOX_PAYMENT_TITLE = "PayPal Checkout - Review your payment";
    static final String PAYPAL_SANDBOX_CHOOSE_WAY_TO_PAY = "PayPal Checkout - Choose a way to pay";
    static final String PAYPAL_ACCOUNT_EMAIL = "sb-maa9e782290@personal.example.com";
    static final String PAYPAL_ACCOUNT_PASSWORD = "@-i3V_u,";

    static final String BANK_CHECKOUT_TITLE = "Bank checkout";
    static final String BANK_CHECKOUT_PAN = "6769737373722567";
    static final String BANK_CHECKOUT_CCV = "808";
    static final String BANK_CHECKOUT_DATE = "12312021";
    static final String BANK_CHECKOUT_NAME = "John Doe";

    static final PaymentItem payPalSuccessfulPayment = PaymentItem.builder()
            .item("Paypal - success")
            .description("Successful payment with paypal service")
            .price(0.05)
            .method(PaymentMethod.PAYPAL)
            .build();

    static final PaymentItem payPalUnsuccessfulPayment = PaymentItem.builder()
            .item("Paypal - failure")
            .description("Unsuccessful payment with paypal service")
            .price(6000d)
            .method(PaymentMethod.PAYPAL)
            .build();

    static final PaymentItem bitcoinSuccessfulPayment = PaymentItem.builder()
            .item("Bitcoin - success")
            .description("Successful payment with bitcoin service")
            .price(0.02)
            .method(PaymentMethod.BITCOIN)
            .build();

    static final PaymentItem bitcoinUnsuccessfulPayment = PaymentItem.builder()
            .item("Bitcoin - failure")
            .description("Unsuccessful payment with bitcoin service")
            .price(0.005)
            .method(PaymentMethod.BITCOIN)
            .build();

    static final PaymentItem bankSuccessfulPayment = PaymentItem.builder()
            .item("Bank - success")
            .description("Successful payment with bank service")
            .price(120d)
            .method(PaymentMethod.BANK)
            .build();

    static final PaymentItem bankUnsuccessfulPayment = PaymentItem.builder()
            .item("Bank - failure")
            .description("Unsuccessful payment with bank service")
            .price(10000d)
            .method(PaymentMethod.BANK)
            .build();

}
