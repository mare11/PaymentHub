package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Client findByMerchantIdAndMerchantPassword(String merchantId, String merchantPassword);

    Client findByFirstNameAndLastNameAndCards_Pan(String firstName, String lastName, String pan);
}
