package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.service.RoomService;
import com.sesac.carematching.config.ApiVersion;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@ApiVersion(1)
public class RoomController_Maria extends RoomController<Integer> {
    private final RoomService<String> roomService_Mongo;

    public RoomController_Maria(RoomService<Integer> roomService, TokenAuth tokenAuth, RoomService<String> roomService_Mongo) {
        super(roomService, tokenAuth);
        this.roomService_Mongo = roomService_Mongo;
    }


    /**
     * 채팅방 생성 (로그인 사용자는 username, 요양사는 caregiverId로 처리)
     */
    @PostMapping
    public ResponseEntity<RoomResponse<Integer>> createRoom(HttpServletRequest request,
                                                      @RequestBody CreateRoomRequest roomRequest) {
        // (1) JWT에서 요청자(username) 추출
        String requesterUsername = tokenAuth.extractUsernameFromToken(request);

        // (2) Service 계층으로 caregiverId & requesterUsername 전달
        RoomResponse<Integer> roomResponse = roomService.createRoom(requesterUsername, roomRequest.getCaregiverId());
        roomService_Mongo.createRoom(requesterUsername, roomRequest.getCaregiverId());

        return ResponseEntity.ok(roomResponse);
    }
}
