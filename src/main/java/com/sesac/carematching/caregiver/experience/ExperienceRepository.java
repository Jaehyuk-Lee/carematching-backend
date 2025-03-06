package com.sesac.carematching.caregiver.experience;

import com.sesac.carematching.caregiver.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Integer> {
    List<Experience> findByCaregiver(Caregiver caregiver);
}
