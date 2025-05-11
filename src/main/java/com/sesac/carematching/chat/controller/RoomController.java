package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.service.RoomService;
import com.sesac.carematching.config.ApiVersion;
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


    /**
     * 채팅방 생성 (로그인 사용자는 username, 요양사는 caregiverId로 처리)
     */
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(HttpServletRequest request,
                                                   @RequestBody CreateRoomRequest roomRequest) {
        // (1) JWT에서 요청자(username) 추출
        String requesterUsername = tokenAuth.extractUsernameFromToken(request);

        // (2) Service 계층으로 caregiverId & requesterUsername 전달
        RoomResponse roomResponse = roomService.createRoom(requesterUsername, roomRequest.getCaregiverId());

        return ResponseEntity.ok(roomResponse);
    }

    /**
     * 사용자가 참여 중인 채팅방 목록 조회 (username 기반)
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getUserRooms(HttpServletRequest request) {
        // 1. JWT 토큰에서 사용자 username 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        System.out.println("🔍 [DEBUG] 추출된 사용자 이름 (JWT 인증): " + username);

        // 2. 참여 중인 채팅방 목록 조회
        List<RoomResponse> rooms = roomService.getUserRooms(username);


        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Integer roomId) {
        RoomResponse roomResponse = roomService.getRoom(roomId);
        return ResponseEntity.ok(roomResponse);
    }
}
