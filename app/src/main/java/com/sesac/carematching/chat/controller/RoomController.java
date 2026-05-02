package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.service.RoomService;
import com.sesac.carematching.config.ApiVersion;
import com.sesac.carematching.exception.VersionException;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Room Controller", description = "채팅방 관리")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final TokenAuth tokenAuth;


    @Operation(summary = "채팅방 생성", description = "요청자와 돌봄이 ID로 채팅방을 생성합니다.")
    @PostMapping
    @ApiVersion(2)
    public ResponseEntity<RoomResponse> createRoom(HttpServletRequest request,
                                                   @RequestBody CreateRoomRequest roomRequest) {
        // (1) JWT에서 요청자(username) 추출
        String requesterUsername = tokenAuth.extractUsernameFromToken(request);

        // (2) Service 계층으로 caregiverId & requesterUsername 전달
        RoomResponse roomResponse = roomService.createRoom(requesterUsername, roomRequest.getCaregiverId());

        return ResponseEntity.ok(roomResponse);
    }
    @PostMapping
    @ApiVersion(1)
    public void createRoomVersionException() {
        throw new VersionException();
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "로그인한 사용자가 참여 중인 채팅방 목록을 조회합니다.")
    @GetMapping
    @ApiVersion(2)
    public ResponseEntity<List<RoomResponse>> getUserRooms(HttpServletRequest request) {
        // 1. JWT 토큰에서 사용자 username 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        System.out.println("🔍 [DEBUG] 추출된 사용자 이름 (JWT 인증): " + username);

        // 2. 참여 중인 채팅방 목록 조회
        List<RoomResponse> rooms = roomService.getUserRooms(username);


        return ResponseEntity.ok(rooms);
    }
    @GetMapping
    @ApiVersion(1)
    public void getUserRoomsVersionException() {
        throw new VersionException();
    }

    @Operation(summary = "채팅방 단건 조회", description = "채팅방 ID로 채팅방 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    @ApiVersion(2)
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String roomId) {
        RoomResponse roomResponse = roomService.getRoom(roomId);
        return ResponseEntity.ok(roomResponse);
    }
    @GetMapping("/{roomId}")
    @ApiVersion(1)
    public void getRoomVersionException() {
        throw new VersionException();
    }
}
