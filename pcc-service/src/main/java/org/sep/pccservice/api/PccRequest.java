package org.sep.pccservice.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PccRequest {
    private String acquirerOrderId;
    private LocalDateTime acquirerTimestamp;
    private Double amount;
    private String pan;
    private String ccv;
    private LocalDate expirationDate;
    private String cardholderName;
}
