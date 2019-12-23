package org.sep.acquirerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {
    private String pan;
    private String ccv;
    private LocalDate expirationDate;
    private String cardholderName;
}
