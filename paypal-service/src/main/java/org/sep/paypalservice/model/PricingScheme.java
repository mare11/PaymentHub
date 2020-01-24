package org.sep.paypalservice.model;

import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
import com.paypal.orders.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Model
public class PricingScheme {

    @SerializedName("fixed_price")
    private Money fixedPrice;
}