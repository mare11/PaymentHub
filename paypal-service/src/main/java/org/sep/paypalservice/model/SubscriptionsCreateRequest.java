package org.sep.paypalservice.model;

public class SubscriptionsCreateRequest extends CreateRequest<Subscription> {

    private static final String SUBSCRIPTIONS_PLAN = "/v1/billing/subscriptions";

    public SubscriptionsCreateRequest() {
        super(SUBSCRIPTIONS_PLAN, Subscription.class);
    }
}