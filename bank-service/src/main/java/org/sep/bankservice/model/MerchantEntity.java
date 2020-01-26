package org.sep.bankservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "merchant")
public class MerchantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String bankMerchantId;

    @Column(nullable = false)
    private String bankMerchantPassword;

    @OneToMany(mappedBy = "merchant")
    private Set<TransactionEntity> transactions;
}
