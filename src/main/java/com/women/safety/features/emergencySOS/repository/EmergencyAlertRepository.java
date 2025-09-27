package com.women.safety.features.emergencySOS.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    List<EmergencyAlert> findByUserOrderByCreatedAtDesc(AuthUser user);

    List<EmergencyAlert> findByUserAndAlertStatusOrderByCreatedAtDesc(AuthUser user, EmergencyAlert.AlertStatus status);

    @Query("SELECT ea FROM EmergencyAlert ea WHERE ea.user = :user AND ea.createdAt >= :fromDate ORDER BY ea.createdAt DESC")
    List<EmergencyAlert> findRecentAlerts(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT ea FROM EmergencyAlert ea WHERE ea.alertStatus = :status AND ea.createdAt >= :fromDate")
    List<EmergencyAlert> findActiveAlertsFromDate(@Param("status") EmergencyAlert.AlertStatus status, @Param("fromDate") LocalDateTime fromDate);
}
