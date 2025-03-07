package com.sesac.carematching.caregiver;

import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CaregiverRepository extends JpaRepository<Caregiver, Integer> {
    boolean existsByUser(User user);
    Optional<Caregiver> findByUser(User user);
    @Query("SELECT c FROM Caregiver c " +
        "JOIN c.user u " +
        "JOIN u.role r " +
        "WHERE c.status = :status " +
        "AND r.rname = :roleName")
    List<Caregiver> findByStatusAndRoleName(
        @Param("status") Status status,
        @Param("roleName") String roleName
    );

    Optional<Caregiver> findById(Integer caregiverId);
}
