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
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String issn;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private String merchantPassword;

    @OneToMany(mappedBy = "merchant")
    private Set<Transaction> transactions;
}
