package com.sesac.carematching.chat.room;

import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoRoomRepository extends MongoRepository<MongoRoom, String> {

    @NonNull
    Optional<MongoRoom> findById(@NonNull String id);

    List<MongoRoom> findByRequesterUserIdOrReceiverUserId(Integer requesterUserId, Integer receiverUserId);

    boolean existsByRequesterUserIdAndReceiverUserId(Integer requesterUserId, Integer receiverUserId);
}
