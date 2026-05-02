package com.sesac.carematching.user;

import com.sesac.carematching.user.dto.StatusDTO;
import com.sesac.carematching.user.dto.UserSignupDTO;
import com.sesac.carematching.user.dto.UserUpdateDTO;
import com.sesac.carematching.util.S3UploadService;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "User Controller", description = "사용자 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TokenAuth tokenAuth;
    private final S3UploadService s3UploadService;

    @Operation(summary = "회원 가입", description = "")
    @PostMapping("/signup")
    public ResponseEntity<Void> join(@RequestBody UserSignupDTO user) {
        System.out.println("회원가입 컨트롤러 실행" + user);
        userService.registerUser(user);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }

    private void checkAdminPrivileges(HttpServletRequest request) {
        User requestedUser = userService.getUserInfo(tokenAuth.extractUsernameFromToken(request));
        if (requestedUser == null || !requestedUser.getRole().getRname().equals("ROLE_ADMIN")) {
            throw new AdminAuthException("관리자 전용 기능입니다.");
        }
    }

    @Operation(summary = "회원 탈퇴")
    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/info")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        return ResponseEntity.ok(userService.getUser(username));
    }

    @Operation(summary = "회원 정보 업데이트")
    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        userService.updateUser(username, userUpdateDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "관리자 - 자격증 리스트 조회")
    @PostMapping("/admin/cert")
    public ResponseEntity<?> getCertList(HttpServletRequest request) {
        checkAdminPrivileges(request);
        return ResponseEntity.ok(userService.getCertList());
    }

    @Operation(summary = "관리자 - 자격증 인증/인증취소")
    @PostMapping("/admin/cert/{username}/pending/update")
    public ResponseEntity<?> updateCertPending(@RequestBody StatusDTO statusDTO, HttpServletRequest request, @PathVariable String username) {
        checkAdminPrivileges(request);
        userService.updatePending(username, statusDTO.isStatus());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 프로필 사진 업로드")
    @PostMapping(value = "/update/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(HttpServletRequest request,
                                                @RequestPart("imageFile") MultipartFile imageFile) {
        // 1) 토큰에서 사용자 이름 추출 후 사용자 정보 조회
        String username = tokenAuth.extractUsernameFromToken(request);
        try {
            User user = userService.getUserInfo(username);
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body("이미지 파일을 첨부해주세요.");
            }
            // 2) 기존 프로필 이미지가 있다면 S3에서 삭제 (옵션)
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                s3UploadService.deleteProfileImageFile(user.getProfileImage());
            }
            // 3) 새 프로필 이미지 S3 업로드 후 URL 반환
            String newProfileImageUrl = s3UploadService.saveProfileImageFile(imageFile);
            // 4) DB의 프로필 이미지 URL 업데이트
            userService.updateProfileImage(username, newProfileImageUrl);
            // 5) 성공 응답 (새로운 프로필 이미지 URL 반환)
            Map<String, String> response = new HashMap<>();
            response.put("profileImage", newProfileImageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("프로필 이미지 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
