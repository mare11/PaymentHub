package org.sep.sellerservice.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Merchant {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private Boolean enabled;

    @Column(nullable = false)
    private String returnUrl;

    @OneToMany(mappedBy = "id.merchant")
    @ToString.Exclude
    private Set<MerchantPaymentMethod> merchantPaymentMethods;
}