package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverListResponse;
import com.sesac.carematching.caregiver.dto.CaregiverResponse;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
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

    @GetMapping
    public ResponseEntity<List<CaregiverListResponse>> CaregiverList() {
        List<CaregiverListResponse> caregivers = caregiverService.findAll()
            .stream()
            .map(CaregiverListResponse::new)
            .toList();
        return ResponseEntity.ok()
            .body(caregivers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaregiverResponse> findCaregiver(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        return ResponseEntity.ok(new CaregiverResponse(caregiver));
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
    public ResponseEntity<CaregiverResponse> addCaregiver(@RequestBody AddCaregiverRequest request) {
        Caregiver savedCaregiver = caregiverService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverResponse(savedCaregiver));
    }
}
