package org.sep.scholar;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class PaymentItem {

    private String item;
    private String description;
    private Double price;
    private PaymentMethod method;
}