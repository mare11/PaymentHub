package org.sep.paypalservice.model;


import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
import com.paypal.orders.Name;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Model
public class Subscriber {

    @SerializedName("name")
    private Name name;
}