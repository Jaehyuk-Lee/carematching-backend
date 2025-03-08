package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.user.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomService {

    RoomResponse getRoom(Integer roomId);
    List<RoomResponse> getUserRooms(Integer id);

    @Transactional(readOnly = true)
    List<RoomResponse> getUserRooms(String username);

    RoomResponse createRoom(String requesterUsername, Integer caregiverId);
}

