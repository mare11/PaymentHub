package org.sep.sellerservice.repository;

import org.sep.sellerservice.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRegistrationRepository extends JpaRepository<Seller, Long> { }