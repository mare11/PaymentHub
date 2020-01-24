package org.sep.paypalservice.model;

import com.paypal.http.HttpRequest;

public class SubscriptionsGetRequest extends HttpRequest<Subscription> {

    private static final String HTTP_GET = "GET";

    public SubscriptionsGetRequest(final String subscriptionId) {
        super("/v1/billing/subscriptions/".concat(subscriptionId).concat("?"), HTTP_GET, Subscription.class);
        this.header("Content-Type", "application/json");
    }
}