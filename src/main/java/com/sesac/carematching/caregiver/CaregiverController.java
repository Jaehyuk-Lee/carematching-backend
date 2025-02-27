package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverListDto;
import com.sesac.carematching.caregiver.dto.CaregiverDetailDto;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverDto;
import com.sesac.carematching.caregiver.dto.AddCaregiverDto;
import com.sesac.carematching.user.role.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sesac.carematching.util.TokenAuth;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregivers")
public class CaregiverController {
    private final CaregiverService caregiverService;
    private final RoleService roleService;
    private final TokenAuth tokenAuth;

    @GetMapping
    public ResponseEntity<List<CaregiverListDto>> CaregiverList() {
        List<CaregiverListDto> caregivers = caregiverService.findALlOpenCaregiver()
            .stream()
            .map(CaregiverListDto::new)
            .toList();
        return ResponseEntity.ok()
            .body(caregivers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaregiverDetailDto> findCaregiverById(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver));
    }

    @GetMapping("/user")
    public ResponseEntity<CaregiverDetailDto> findCaregiverByUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = caregiverService.findByUsername(username);
        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver));
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCaregiver(@PathVariable Integer id) {
        caregiverService.delete(id);
        return ResponseEntity.ok()
            .build();
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<CaregiverDetailDto> updateCaregiver(@PathVariable Integer id,
                                                              @RequestBody UpdateCaregiverDto dto) {
        Caregiver updatedCaregiver = caregiverService.update(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverDetailDto(updatedCaregiver));
    }

    @PostMapping("/add")
    public ResponseEntity<CaregiverDetailDto> addCaregiver(@RequestBody AddCaregiverDto dto,
                                                           HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver savedCaregiver = caregiverService.save(username, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverDetailDto(savedCaregiver));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCaregiver(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        boolean isRegistered = caregiverService.isCaregiverRegistered(username);
        return ResponseEntity.ok(isRegistered);
    }
}
