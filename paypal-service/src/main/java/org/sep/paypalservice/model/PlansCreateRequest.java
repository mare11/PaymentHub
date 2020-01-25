package org.sep.paypalservice.model;

public class PlansCreateRequest extends CreateRequest<Plan> {

    private static final String PLANS_PATH = "/v1/billing/plans";

    public PlansCreateRequest() {
        super(PLANS_PATH, Plan.class);
    }
}