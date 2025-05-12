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
@ApiVersion(2)
public class RoomController_Mongo extends RoomController<String> {
    public RoomController_Mongo(RoomService<String> roomService, TokenAuth tokenAuth) {
        super(roomService, tokenAuth);
    }


    /**
     * 채팅방 생성 (로그인 사용자는 username, 요양사는 caregiverId로 처리)
     */
    @PostMapping
    public ResponseEntity<RoomResponse<String>> createRoom(HttpServletRequest request,
                                                      @RequestBody CreateRoomRequest roomRequest) {
        // (1) JWT에서 요청자(username) 추출
        String requesterUsername = tokenAuth.extractUsernameFromToken(request);

        // (2) Service 계층으로 caregiverId & requesterUsername 전달
        RoomResponse<String> roomResponse = roomService.createRoom(requesterUsername, roomRequest.getCaregiverId());

        return ResponseEntity.ok(roomResponse);
    }
}
