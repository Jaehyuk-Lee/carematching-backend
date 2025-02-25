package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverResponse;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregivers")
public class CaregiverController {
    private final CaregiverService caregiverService;

    @GetMapping("/get/{id}")
    public ResponseEntity<CaregiverResponse> findCaregiver(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        return ResponseEntity.ok()
            .body(new CaregiverResponse(caregiver));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCaregiver(@PathVariable Integer id) {
        caregiverService.delete(id);
        return ResponseEntity.ok()
            .build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Caregiver> updateCaregiver(@PathVariable Integer id,
                                                 @RequestBody UpdateCaregiverRequest request) {
        Caregiver updatedCaregiver = caregiverService.update(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(updatedCaregiver);
    }

    @PostMapping("/add")
    public ResponseEntity<Caregiver> addCaregiver(@RequestBody AddCaregiverRequest request) {
        Caregiver savedCaregiver = caregiverService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(savedCaregiver);
    }
}
