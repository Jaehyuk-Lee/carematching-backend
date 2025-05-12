package com.sesac.carematching.chat.room;

import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    List<Room> findByRequesterOrReceiver(User requester, User receiver);

    boolean existsByRequesterAndReceiver(User requester, User receiver);

    // 🔒 요청자와 수신자가 바뀐 경우도 중복 체크
    boolean existsByRequesterAndReceiverOrRequesterAndReceiver(User requester, User receiver, User receiver2, User requester2);
}
