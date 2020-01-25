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
public class Product {

    @SerializedName("id")
    private String productId;
    @SerializedName("name")
    private String name;
    @SerializedName("type")
    private String type;
    @SerializedName("category")
    private String category;
}