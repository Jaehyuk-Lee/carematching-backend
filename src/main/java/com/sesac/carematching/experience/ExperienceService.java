package com.sesac.carematching.experience;

import com.sesac.carematching.caregiver.Caregiver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExperienceService {
    private final ExperienceRepository experienceRepository;
    public Experience save(ExperienceRequest dto, Caregiver caregiver) {
        return experienceRepository.save(toEntity(dto, caregiver));
    }

    @Transactional
    public Experience update(ExperienceRequest dto, Integer id) {
        Experience experience = experienceRepository.findById(id).orElseThrow(
            ()->new IllegalArgumentException("Experience is null")
        );
        experience.setTitle(dto.getTitle());
        experience.setSummary(dto.getSummary());
        experience.setLocation(dto.getLocation());
        return experienceRepository.save(experience);
    }

    public List<Experience> findExperienceList(Caregiver caregiver) {
        return experienceRepository.findByCaregiver(caregiver).orElse(null);
    }

    public Experience toEntity(ExperienceRequest dto, Caregiver caregiver) {
        return Experience.builder()
            .title(dto.getTitle())
            .summary(dto.getSummary())
            .location(dto.getLocation())
            .build();
    }
}
