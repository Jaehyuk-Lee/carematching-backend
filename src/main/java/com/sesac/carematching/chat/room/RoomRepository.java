package com.sesac.carematching.chat.room;

import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {

    @NonNull
    Optional<Room> findById(@NonNull String id);

    List<Room> findByRequesterUserIdOrReceiverUserId(Integer requesterUserId, Integer receiverUserId);

    boolean existsByRequesterUserIdAndReceiverUserId(Integer requesterUserId, Integer receiverUserId);
}
