package org.sep.paypalservice.model;


import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Model
public class BillingCycle {

    @SerializedName("pricing_scheme")
    private PricingScheme pricingScheme;
    @SerializedName("frequency")
    private Frequency frequency;
    @SerializedName("tenure_type")
    private String tenureType;
    @SerializedName("sequence")
    private Integer sequence;
    @SerializedName("total_cycles")
    private Integer totalCycles;
}