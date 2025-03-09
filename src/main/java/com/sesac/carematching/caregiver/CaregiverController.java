package com.sesac.carematching.caregiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.caregiver.dto.CaregiverListDto;
import com.sesac.carematching.caregiver.dto.CaregiverDetailDto;
import com.sesac.carematching.caregiver.dto.BuildCaregiverDto;
import com.sesac.carematching.caregiver.experience.Experience;
import com.sesac.carematching.caregiver.experience.ExperienceRequest;
import com.sesac.carematching.caregiver.experience.ExperienceService;
import com.sesac.carematching.caregiver.review.ReviewService;
import com.sesac.carematching.user.role.RoleService;
import com.sesac.carematching.util.S3UploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sesac.carematching.util.TokenAuth;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregivers")
@Log4j2
public class CaregiverController {
    private final CaregiverService caregiverService;
    private final CaregiverRepository caregiverRepository;
    private final ExperienceService experienceService;
    private final ReviewService reviewService;
    private final TokenAuth tokenAuth;
    private final S3UploadService s3UploadService;

    @GetMapping
    public ResponseEntity<List<CaregiverListDto>> getCaregiverList() {
        List<Caregiver> caregivers = caregiverService.findAllOpenCaregiver();

        // Controller에서 DTO 변환 수행
        List<CaregiverListDto> caregiverDtos = caregivers.stream()
            .map(caregiver -> new CaregiverListDto(caregiver, reviewService.count(caregiver)))
            .toList();

        return ResponseEntity.ok(caregiverDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaregiverDetailDto> findCaregiverById(@PathVariable Integer id) {
        Caregiver caregiver = caregiverService.findById(id);
        List<Experience> experiences = experienceService.findExperienceList(caregiver);
        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver, experiences));
    }

    @GetMapping("/user")
    public ResponseEntity<CaregiverDetailDto> findCaregiverByUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        Caregiver caregiver = caregiverService.findByUsername(username);
        log.info("caregiverId: "+ caregiver.getId());
        List<Experience> experiences = experienceService.findExperienceList(caregiver);
        return ResponseEntity.ok()
            .body(new CaregiverDetailDto(caregiver, experiences));
    }


    @GetMapping("/{caregiverId}/userId")
    public ResponseEntity<Integer> getCaregiverUserId(@PathVariable Integer caregiverId) {
        return caregiverService.findUserIdByCaregiverId(caregiverId)
            .map(ResponseEntity::ok)  // Optional<Integer> → ResponseEntity<Integer>
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/build", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CaregiverDetailDto> buildCaregiver(HttpServletRequest request,
                                                             @RequestPart("caregiverDto") BuildCaregiverDto dto,
                                                             @RequestPart(value = "caregiverImage", required = false) MultipartFile imageFile) throws IOException {
        String username = tokenAuth.extractUsernameFromToken(request);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = s3UploadService.saveCaregiverImageFile(imageFile);
            dto.setCaregiverImage(imageUrl);
        }

        Caregiver caregiver = updateOrCreateCaregiver(username, dto);
        List<Experience> experiences = processExperienceList(caregiver, dto.getExperienceList());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CaregiverDetailDto(caregiver, experiences));
    }

    private Caregiver updateOrCreateCaregiver(String username, BuildCaregiverDto dto) {
        return Optional.ofNullable(caregiverService.findByUsername(username))
            .map(existingCaregiver -> caregiverService.update(username, dto))
            .orElseGet(() -> caregiverService.save(username, dto));
    }

    private List<Experience> processExperienceList(Caregiver caregiver, List<ExperienceRequest> experienceRequestList) {
        if (experienceRequestList == null || experienceRequestList.isEmpty()) return List.of();

        List<Experience> existingExperiences = caregiver.getExperienceList();

        int existingSize = existingExperiences.size();
        int newSize = experienceRequestList.size();

        // 기존 경험 업데이트
        IntStream.range(0, Math.min(existingSize, newSize))
            .forEach(i -> experienceService.update(experienceRequestList.get(i), existingExperiences.get(i).getId()));

        // 추가 경험 저장 (새로운 데이터가 더 많은 경우)
        List<Experience> newExperiences = experienceRequestList.subList(existingSize, newSize).stream()
            .map(ex -> experienceService.save(ex, caregiver))
            .collect(Collectors.toList());

        // 기존 경험 + 새로 추가된 경험 리스트 반환
        List<Experience> allExperiences = new ArrayList<>(existingExperiences);
        allExperiences.addAll(newExperiences);

        return allExperiences.isEmpty() ? List.of() : allExperiences;
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkCaregiver(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        boolean isRegistered = caregiverService.isCaregiverRegistered(username);
        return ResponseEntity.ok(isRegistered);
    }
}
