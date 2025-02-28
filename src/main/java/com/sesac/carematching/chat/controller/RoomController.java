package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.service.RoomService;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final TokenAuth tokenAuth;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(HttpServletRequest request, @RequestBody CreateRoomRequest roomRequest) {
        System.out.println("🔍 [DEBUG] 받은 요청 데이터: " + roomRequest);

        // 1. JWT 토큰에서 사용자 이름(username) 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        System.out.println("🔍 [DEBUG] 추출된 사용자 이름: " + username);

        // 2. username을 사용하여 User ID 조회
        User requester = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        System.out.println("🔍 [DEBUG] 요청자 ID: " + requester.getId());

        // 3. Room 생성 요청 시 userId를 설정
        roomRequest.setRequesterUserId(requester.getId());

        // 4. Room 생성
        RoomResponse roomResponse = roomService.createRoom(roomRequest);
        return ResponseEntity.ok(roomResponse);
    }


    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Integer roomId) {
        RoomResponse roomResponse = roomService.getRoom(roomId);
        return ResponseEntity.ok(roomResponse);
    }
}
