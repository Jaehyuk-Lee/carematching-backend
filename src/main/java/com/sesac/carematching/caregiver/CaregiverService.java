package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.BuildCaregiverDto;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CaregiverService {
    private final CaregiverRepository caregiverRepository;
    private final UserRepository userRepository;

    @Transactional
    public Caregiver save(String username, BuildCaregiverDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("User must not be null"));
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID must not be null");
        }
        return caregiverRepository.save(toEntity(dto, user));
    }

    public Caregiver findById(Integer id) {
        return caregiverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + id));
    }

    public Caregiver findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()->new IllegalArgumentException("User is null"));
        return  caregiverRepository.findByUser(user)
            .orElse(null);
    }

//    public void delete(Integer id) {
//        caregiverRepository.deleteById(id);
//    }

    @Transactional
    public Caregiver update(String username, BuildCaregiverDto dto) {
        Caregiver caregiver = findByUsername(username);
        caregiver.setLoc(dto.getLoc());
        caregiver.setServNeeded(dto.getServNeeded());
        caregiver.setWorkDays(dto.getWorkDays());
        caregiver.setWorkTime(dto.getWorkTime());
        caregiver.setWorkForm(dto.getWorkForm());
        caregiver.setEmploymentType(dto.getEmploymentType());
        caregiver.setSalary(dto.getSalary());
        caregiver.setStatus(dto.getStatus());
        return caregiverRepository.save(caregiver);
    }

    public List<Caregiver> findALlOpenCaregiver() {
        return caregiverRepository.findByStatusAndRoleName(Status.OPEN, "ROLE_USER_CAREGIVER");
    }

    public boolean isCaregiverRegistered(String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        if (user == null) {
            return false;
        }
        return caregiverRepository.existsByUser(user);
    }

    private Caregiver toEntity(BuildCaregiverDto dto, User user) {
        return Caregiver.builder()
            .user(user)
            .loc(dto.getLoc())
            .realName(dto.getRealName())
            .servNeeded(dto.getServNeeded())
            .workDays(dto.getWorkDays())
            .workTime(dto.getWorkTime())
            .workForm(dto.getWorkForm())
            .employmentType(dto.getEmploymentType())
            .salary(dto.getSalary())
            .status(dto.getStatus())
            .build();
    }
}
