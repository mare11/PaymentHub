package org.sep.paypalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {

    private String merchantId;
    private String clientId;
    private String clientSecret;
    private Boolean subscription;
    private Double setupFee;
    private List<PlanDto> plans;
}