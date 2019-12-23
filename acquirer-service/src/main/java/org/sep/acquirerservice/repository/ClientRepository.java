package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByMerchantId(String merchantId);

//    @Query() todo
//    Boolean isOwnerOfTheCard(Card card);
}
