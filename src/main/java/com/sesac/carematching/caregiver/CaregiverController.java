package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverListResponse;
import com.sesac.carematching.caregiver.dto.CaregiverResponse;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.user.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregivers")
public class CaregiverController {
    private final CaregiverService caregiverService;
    private final RoleService roleService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CaregiverListResponse>> CaregiverList() {
        List<CaregiverListResponse> caregivers = caregiverService.findALlOpen()
            .stream()
            .map(CaregiverListResponse::new)
            .toList();
        return ResponseEntity.ok()
            .body(caregivers);
    }

    @GetMapping("/caregiver/{id}")
    public ResponseEntity<CaregiverResponse> findCaregiverById(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        return ResponseEntity.ok()
            .body(new CaregiverResponse(caregiver));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<CaregiverResponse> findCaregiverByUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()->new IllegalArgumentException("User is null"));
        Caregiver caregiver = caregiverService.findByUser(user);
        return ResponseEntity.ok()
            .body(new CaregiverResponse(caregiver));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCaregiver(@PathVariable Integer id) {
        caregiverService.delete(id);
        return ResponseEntity.ok()
            .build();
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<CaregiverResponse> updateCaregiver(@PathVariable Integer id,
                                                 @RequestBody UpdateCaregiverRequest request) {
        Caregiver updatedCaregiver = caregiverService.update(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverResponse(updatedCaregiver));
    }

    @PostMapping("/add")
    public ResponseEntity<CaregiverResponse> addCaregiver(@RequestHeader String username,
                                                          @RequestBody AddCaregiverRequest request) {
        roleService.changeRoleToCaregiver(username);
        Caregiver savedCaregiver = caregiverService.save(username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverResponse(savedCaregiver));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCaregiver(@RequestHeader String username) {
        boolean isRegistered = caregiverService.isCaregiverRegistered(username);
        return ResponseEntity.ok(isRegistered);
    }}
