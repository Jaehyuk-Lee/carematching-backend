package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CaregiverService {
    private final CaregiverRepository caregiverRepository;

    @Transactional
    public Caregiver save(AddCaregiverRequest request) {
        User user = request.getUser();

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID must not be null");
        }
        return caregiverRepository.save(request.toEntity());
    }

    public Caregiver findById(Integer id) {
        return caregiverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + id));
    }

    public void delete(Integer id) {
        caregiverRepository.deleteById(id);
    }

    @Transactional
    public Caregiver update(Integer id, UpdateCaregiverRequest request) {
        Caregiver caregiver = findById(id);
        caregiver.setLoc(request.getLoc());
        caregiver.setServNeeded(request.getServNeeded());
        caregiver.setWorkDays(request.getWorkDays());
        caregiver.setWorkTime(request.getWorkTime());
        caregiver.setWorkForm(request.getWorkForm());
        caregiver.setEmploymentType(request.getEmploymentType());
        caregiver.setSalary(request.getSalary());
        caregiver.setStatus(request.getStatus());
        return caregiverRepository.save(caregiver);
    }
}
