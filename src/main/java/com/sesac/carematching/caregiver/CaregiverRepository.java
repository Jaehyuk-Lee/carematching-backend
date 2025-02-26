package com.sesac.carematching.caregiver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {
    List<Caregiver> findByStatus(Status status);
}
