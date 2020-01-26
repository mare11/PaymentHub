package org.sep.paypalservice.model;


import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Model
public class Subscription {

    @SerializedName("id")
    private String id;
    @SerializedName("status")
    private String status;
    @SerializedName("plan_id")
    private String planId;
    @SerializedName("subscriber")
    private Subscriber subscriber;
    @SerializedName("application_context")
    private ApplicationContext applicationContext;
    @SerializedName(value = "links", listClass = LinkDescription.class)
    private List<LinkDescription> links;
}