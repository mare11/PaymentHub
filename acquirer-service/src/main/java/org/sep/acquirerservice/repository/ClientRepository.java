package org.sep.acquirerservice.repository;

import org.sep.acquirerservice.model.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    ClientEntity findByFirstNameAndLastNameAndCards_Pan(String firstName, String lastName, String pan);
}
