package org.sep.paypalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sep.paypalservice.model.IntervalUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {

    private String plan;
    private IntervalUnit intervalUnit;
    private Integer intervalCount;
    private Double price;
}