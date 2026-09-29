package com.sece.housekeeptrack.repository;

import com.sece.housekeeptrack.entity.Housekeeper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HousekeeperRepository extends JpaRepository<Housekeeper, Long> {
    Optional<Housekeeper> findFirstByAvailableTrue();
}
