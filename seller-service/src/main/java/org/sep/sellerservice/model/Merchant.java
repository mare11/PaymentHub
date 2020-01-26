package org.sep.sellerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Merchant {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private Boolean enabled;

    @Column(nullable = false)
    private String returnUrl;

    @ManyToMany
    @JoinTable(name = "merchant_payment_methods",
            joinColumns = {@JoinColumn(name = "merchant_id")},
            inverseJoinColumns = {@JoinColumn(name = "method_id")})
    private Set<PaymentMethodEntity> paymentMethodEntities;
}