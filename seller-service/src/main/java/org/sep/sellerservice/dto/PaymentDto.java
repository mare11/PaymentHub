package org.sep.sellerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {

    private Long id;
    private String item;
    private Integer amount;
    private Double price;
    private SellerDto seller;
}