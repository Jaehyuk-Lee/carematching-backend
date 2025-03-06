package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.CaregiverListDto;
import com.sesac.carematching.caregiver.dto.CaregiverDetailDto;
import com.sesac.carematching.caregiver.dto.BuildCaregiverDto;
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

    @PostMapping("/build")
    public ResponseEntity<CaregiverDetailDto> buildCaregiver(HttpServletRequest request,
                                                             @RequestBody BuildCaregiverDto dto) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = caregiverService.findByUsername(username);
        if (caregiver != null) {
            caregiver = caregiverService.update(username, dto);
        } else {
            caregiver = caregiverService.save(username, dto);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverDetailDto(caregiver));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCaregiver(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        boolean isRegistered = caregiverService.isCaregiverRegistered(username);
        return ResponseEntity.ok(isRegistered);
    }
}
