package com.women.safety.features.emergencySOS.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.emergencySOS.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByUserOrderByCreatedAtAsc(AuthUser user);

//    List<EmergencyContact> findByUserAndIsPrimaryTrue(AuthUser user);

    @Query("SELECT COUNT(ec) FROM EmergencyContact ec WHERE ec.user = :user")
    long countByUser(@Param("user") AuthUser user);

    boolean existsByUserAndPhoneNumber(AuthUser user, String phoneNumber);
}
