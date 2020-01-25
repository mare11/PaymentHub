package org.sep.paypalservice.model;


import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
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
public class Plan {

    @SerializedName("id")
    private String id;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("name")
    private String name;
    @SerializedName("status")
    private String status;
    @SerializedName(value = "billing_cycles", listClass = BillingCycle.class)
    List<BillingCycle> billingCycles;
    @SerializedName("payment_preferences")
    private PaymentPreferences paymentPreferences;
}