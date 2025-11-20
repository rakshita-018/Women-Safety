package com.women.safety.features.liveLocationTracking.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import com.women.safety.features.liveLocationTracking.model.LocationTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationTrackingRepository extends JpaRepository<LocationTracking, Long> {

    List<LocationTracking> findByUserOrderByTimestampDesc(AuthUser user);

    List<LocationTracking> findByAlertOrderByTimestampDesc(EmergencyAlert alert);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.user = :user AND lt.timestamp >= :fromDate ORDER BY lt.timestamp DESC")
    List<LocationTracking> findRecentLocations(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.user = :user AND lt.timestamp BETWEEN :startDate AND :endDate ORDER BY lt.timestamp ASC")
    List<LocationTracking> findLocationHistory(@Param("user") AuthUser user,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.user = :user ORDER BY lt.timestamp DESC LIMIT 1")
    Optional<LocationTracking> findLatestLocation(@Param("user") AuthUser user);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.alert = :alert ORDER BY lt.timestamp DESC LIMIT 1")
    Optional<LocationTracking> findLatestLocationByAlert(@Param("alert") EmergencyAlert alert);

    @Query("SELECT COUNT(lt) FROM LocationTracking lt WHERE lt.user = :user AND lt.timestamp >= :fromDate")
    long countLocationUpdates(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    // Delete old location data (for privacy/GDPR compliance)
    @Query("DELETE FROM LocationTracking lt WHERE lt.timestamp < :beforeDate")
    void deleteOldLocations(@Param("beforeDate") LocalDateTime beforeDate);
}
