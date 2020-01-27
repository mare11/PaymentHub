package org.sep.paypalservice.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SubscriptionsCancelRequest extends PostRequest<Subscription> {

    private static final String SUBSCRIPTIONS_PLAN = "/v1/billing/subscriptions/{subscription_id}/cancel";

    public SubscriptionsCancelRequest(final String subscriptionId) {
        super(SUBSCRIPTIONS_PLAN, Subscription.class);
        this.path(this.path().replace("{subscription_id}", URLEncoder.encode(String.valueOf(subscriptionId), StandardCharsets.UTF_8)));
    }
}