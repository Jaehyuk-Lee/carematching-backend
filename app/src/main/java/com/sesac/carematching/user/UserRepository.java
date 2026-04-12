package com.sesac.carematching.user;

import com.sesac.carematching.user.dto.UserCertListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    @Query("SELECT new com.sesac.carematching.user.dto.UserCertListDTO(u.username, u.nickname, u.certno, u.pending) FROM User u WHERE u.certno IS NOT NULL")
    List<UserCertListDTO> findCert();
}
