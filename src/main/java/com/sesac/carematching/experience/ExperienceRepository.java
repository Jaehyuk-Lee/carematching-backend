package com.sesac.carematching.experience;

import com.sesac.carematching.caregiver.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Integer> {
    List<Experience> findByCaregiver(Caregiver caregiver);
}
