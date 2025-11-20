package com.women.safety.features.liveLocationTracking.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.liveLocationTracking.model.ScheduledLocationSharing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledLocationSharingRepository extends JpaRepository<ScheduledLocationSharing, Long> {

    List<ScheduledLocationSharing> findByUserOrderByStartTimeDesc(AuthUser user);

    List<ScheduledLocationSharing> findByUserAndStatus(AuthUser user, ScheduledLocationSharing.SharingStatus status);

    @Query("SELECT sls FROM ScheduledLocationSharing sls WHERE sls.user = :user " +
            "AND sls.status = 'ACTIVE' ORDER BY sls.startTime DESC")
    List<ScheduledLocationSharing> findActiveSharing(@Param("user") AuthUser user);

    @Query("SELECT sls FROM ScheduledLocationSharing sls WHERE sls.status = 'SCHEDULED' " +
            "AND sls.startTime <= :now AND sls.endTime >= :now")
    List<ScheduledLocationSharing> findSessionsToActivate(@Param("now") LocalDateTime now);

    @Query("SELECT sls FROM ScheduledLocationSharing sls WHERE sls.status = 'ACTIVE' " +
            "AND sls.endTime < :now")
    List<ScheduledLocationSharing> findExpiredActiveSessions(@Param("now") LocalDateTime now);

    @Query("SELECT sls FROM ScheduledLocationSharing sls WHERE sls.status = 'ACTIVE' " +
            "AND sls.autoAlertIfNotArrived = true " +
            "AND sls.expectedArrivalTime < :delayedTime " +
            "AND sls.actualArrivalTime IS NULL")
    List<ScheduledLocationSharing> findDelayedSessions(@Param("delayedTime") LocalDateTime delayedTime);
}
