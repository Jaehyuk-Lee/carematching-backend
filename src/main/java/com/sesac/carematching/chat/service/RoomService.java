package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.RoomResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomService<T> {

    RoomResponse<T> getRoom(T roomId);

    @Transactional(readOnly = true)
    List<RoomResponse<T>> getUserRooms(String username);

    RoomResponse<T> createRoom(String requesterUsername, Integer caregiverId);
}
