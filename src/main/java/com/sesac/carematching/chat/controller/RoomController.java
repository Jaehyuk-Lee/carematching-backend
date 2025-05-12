package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.service.RoomService;
import com.sesac.carematching.config.ApiVersion;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class RoomController<T> {

    protected final RoomService<T> roomService;
    protected final TokenAuth tokenAuth;

    public RoomController(RoomService<T> roomService, TokenAuth tokenAuth) {
        this.roomService = roomService;
        this.tokenAuth = tokenAuth;
    }

    /**
     * 사용자가 참여 중인 채팅방 목록 조회 (username 기반)
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse<T>>> getUserRooms(HttpServletRequest request) {
        // 1. JWT 토큰에서 사용자 username 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        System.out.println("🔍 [DEBUG] 추출된 사용자 이름 (JWT 인증): " + username);

        // 2. 참여 중인 채팅방 목록 조회
        List<RoomResponse<T>> rooms = roomService.getUserRooms(username);


        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse<T>> getRoom(@PathVariable T roomId) {
        RoomResponse<T> roomResponse = roomService.getRoom(roomId);
        return ResponseEntity.ok(roomResponse);
    }
}
