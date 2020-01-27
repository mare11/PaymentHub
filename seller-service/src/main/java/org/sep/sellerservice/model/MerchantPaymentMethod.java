package org.sep.sellerservice.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class MerchantPaymentMethod {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private MerchantPaymentMethodId id;

    @Column
    private Boolean credentialsProvided;

    @Column
    private String registrationUrl;
}