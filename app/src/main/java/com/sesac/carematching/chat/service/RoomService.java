package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.RoomResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomService {

    RoomResponse getRoom(String roomId);

    @Transactional(readOnly = true)
    List<RoomResponse> getUserRooms(String username);

    RoomResponse createRoom(String requesterUsername, Integer caregiverId);
}
