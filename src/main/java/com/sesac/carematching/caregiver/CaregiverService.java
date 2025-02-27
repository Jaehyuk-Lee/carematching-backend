package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CaregiverService {
    private final CaregiverRepository caregiverRepository;
    private final UserRepository userRepository;

    @Transactional
    public Caregiver save(@RequestHeader String username, // 🔥 헤더에서 username 받기
                          @RequestBody AddCaregiverRequest request) {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new IllegalArgumentException("User must not be null"));
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID must not be null");
        }
        return caregiverRepository.save(toEntity(request, user));
    }

    public Caregiver findById(Integer id) {
        return caregiverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with id: " + id));
    }

    public Caregiver findByUser(User user) {
        return caregiverRepository.findByUser(user)
            .orElseThrow(() -> new EntityNotFoundException("Caregiver not found with username: " + user.getUsername()));
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

    public List<Caregiver> findALlOpen() {
        return caregiverRepository.findByStatus(Status.OPEN);
    }

    public boolean isCaregiverRegistered(String username) {
        User user = userRepository.findByUsername(username)
            .orElse(null);
        if (user == null) {
            return false;
        }
        return caregiverRepository.existsByUser(user);
    }

    private Caregiver toEntity(AddCaregiverRequest request, User user) {
        return Caregiver.builder()
            .user(user)
            .loc(request.getLoc())
            .realName(request.getRealName())
            .servNeeded(request.getServNeeded())
            .workDays(request.getWorkDays())
            .workTime(request.getWorkTime())
            .workForm(request.getWorkForm())
            .employmentType(request.getEmploymentType())
            .salary(request.getSalary())
            .status(request.getStatus())
            .build();
    }
}
