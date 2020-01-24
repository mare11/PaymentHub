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
public class PaymentPreferences {

    @SerializedName("auto_bill_outstanding")
    private Boolean autoBillOutstanding;
    @SerializedName("setup_fee")
    private Money setupFee;
    @SerializedName("setup_fee_failure_action")
    private String setupFeeFailureAction;
    @SerializedName("payment_failure_threshold")
    private Integer paymentFailureThreshold;
}