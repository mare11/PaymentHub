package org.sep.paypalservice.repository;

import org.sep.paypalservice.model.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanEntityRepository extends JpaRepository<PlanEntity, Long> {
}