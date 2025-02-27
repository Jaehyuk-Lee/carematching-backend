package com.sesac.carematching.user;

import com.sesac.carematching.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import util.TokenAuth;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private TokenAuth tokenAuth;

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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        try {
            String username = tokenAuth.extractUsernameFromToken(request);
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
            String username = tokenAuth.extractUsernameFromToken(request);
            userService.updateUser(username, userUpdateDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
