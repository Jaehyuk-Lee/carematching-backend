package com.sesac.carematching.user;

import com.sesac.carematching.config.JwtUtil;
import com.sesac.carematching.user.dto.UserSignupDTO;
import com.sesac.carematching.user.dto.UserUpdateDTO;
import com.sesac.carematching.user.dto.UsernameDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Void> join(@RequestBody UserSignupDTO user) {
        System.out.println("회원가입 컨트롤러 실행" + user);
        userService.registerUser(user);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/loginOk")
    public ResponseEntity<Map<String, String>> loginOk() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        Map<String, String> response = new HashMap<>();
        response.put("email", username);
        response.put("authorities", authorities);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logoutOk")
    public ResponseEntity<Void> logoutOk() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<User> getUserPage() {
        System.out.println("일반 인증 성공");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 유저 정보
        User user = userService.getUserInfo(username);

        return ResponseEntity.ok(user);
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        return username;
    }

    private void checkAdminPrivileges(HttpServletRequest request) {
        User requestedUser = userRepository.findByUsername(extractUsernameFromToken(request)).orElse(null);
        if (requestedUser == null || !requestedUser.getRole().getRname().equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("관리자 전용 기능입니다.");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        try {
            String username = extractUsernameFromToken(request);
            userService.deleteUser(username);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        try {
            String username = extractUsernameFromToken(request);
            userService.updateUser(username, userUpdateDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert")
    public ResponseEntity<?> createAdminCert(HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.getCertList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("목록을 불러오는 도중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert/approve")
    public ResponseEntity<?> createAdminCertApprove(@RequestBody UsernameDTO usernameDTO, HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.updatePending(usernameDTO, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("처리하는 도중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert/revoke")
    public ResponseEntity<?> createAdminCertRevoke(@RequestBody UsernameDTO usernameDTO, HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.updatePending(usernameDTO, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("처리하는 도중 오류가 발생했습니다.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
