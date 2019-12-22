package org.sep.sellerservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String item;
    @Column(nullable = false)
    private Integer amount;
    @Column(nullable = false)
    private Double price;
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
}