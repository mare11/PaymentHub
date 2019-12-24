package org.sep.sellerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDto {
    private Long id;
    private String name;
    private String issn;
    private Boolean enabled;
}