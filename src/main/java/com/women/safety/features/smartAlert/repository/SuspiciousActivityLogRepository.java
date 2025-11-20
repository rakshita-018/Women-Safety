package com.women.safety.features.smartAlert.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.smartAlert.model.SuspiciousActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SuspiciousActivityLogRepository extends JpaRepository<SuspiciousActivityLog, Long> {

    List<SuspiciousActivityLog> findByUserOrderByTimestampDesc(AuthUser user);

    @Query("SELECT sal FROM SuspiciousActivityLog sal WHERE sal.user = :user AND sal.timestamp >= :fromDate ORDER BY sal.timestamp DESC")
    List<SuspiciousActivityLog> findRecentActivities(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT sal FROM SuspiciousActivityLog sal WHERE sal.user = :user AND sal.activityType = :activityType AND sal.timestamp >= :fromDate")
    List<SuspiciousActivityLog> findByActivityType(@Param("user") AuthUser user,
                                                   @Param("activityType") SuspiciousActivityLog.ActivityType activityType,
                                                   @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT sal FROM SuspiciousActivityLog sal WHERE sal.user = :user AND sal.alertTriggered = true ORDER BY sal.timestamp DESC")
    List<SuspiciousActivityLog> findTriggeredAlerts(@Param("user") AuthUser user);

    @Query("SELECT COUNT(sal) FROM SuspiciousActivityLog sal WHERE sal.user = :user AND sal.timestamp >= :fromDate")
    long countRecentActivities(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT sal FROM SuspiciousActivityLog sal WHERE sal.user = :user AND sal.timestamp >= :fromDate AND sal.intensityLevel >= :minIntensity ORDER BY sal.intensityLevel DESC")
    List<SuspiciousActivityLog> findHighIntensityActivities(@Param("user") AuthUser user,
                                                            @Param("fromDate") LocalDateTime fromDate,
                                                            @Param("minIntensity") Double minIntensity);
}
