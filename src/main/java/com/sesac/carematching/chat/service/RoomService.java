package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest createRoomRequest);


    RoomResponse getRoom(Integer roomId);

    List<RoomResponse> getUserRooms(Integer id);
}

