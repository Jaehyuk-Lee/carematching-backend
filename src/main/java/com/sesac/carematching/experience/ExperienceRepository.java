package com.sesac.carematching.experience;

import com.sesac.carematching.caregiver.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Integer> {
    Optional<List<Experience>> findByCaregiver(Caregiver caregiver);
}
