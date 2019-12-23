package org.sep.acquirerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pan;

    @Column(nullable = false)
    private String ccv;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Double availableAmount;

    @Column(nullable = false)
    private Double reservedAmount;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Client client;
}
