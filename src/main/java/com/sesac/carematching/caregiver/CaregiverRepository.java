package com.sesac.carematching.caregiver;

import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {
    List<Caregiver> findByStatus(Status status);
    boolean existsByUser(User user);
    Optional<Caregiver> findByUser(User user);
}
