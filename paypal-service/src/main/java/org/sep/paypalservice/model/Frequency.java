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
public class Frequency {

    @SerializedName("interval_unit")
    private String intervalUnit;
    @SerializedName("interval_count")
    private Integer intervalCount;
}