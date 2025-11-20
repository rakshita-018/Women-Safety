package com.women.safety.features.emergencyMediaFiles.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.emergencyMediaFiles.model.EmergencyMedia;
import com.women.safety.features.emergencySOS.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyMediaRepository extends JpaRepository<EmergencyMedia, Long> {

    List<EmergencyMedia> findByAlertOrderByUploadedAtDesc(EmergencyAlert alert);

    List<EmergencyMedia> findByAlertAndMediaType(EmergencyAlert alert, EmergencyMedia.MediaType mediaType);

    List<EmergencyMedia> findByUserOrderByUploadedAtDesc(AuthUser user);

    @Query("SELECT em FROM EmergencyMedia em WHERE em.alert = :alert AND em.uploadStatus = :status")
    List<EmergencyMedia> findByAlertAndStatus(@Param("alert") EmergencyAlert alert,
                                              @Param("status") EmergencyMedia.UploadStatus status);

    @Query("SELECT COUNT(em) FROM EmergencyMedia em WHERE em.alert = :alert AND em.mediaType = :type")
    long countByAlertAndType(@Param("alert") EmergencyAlert alert,
                             @Param("type") EmergencyMedia.MediaType type);

    @Query("SELECT COUNT(em) FROM EmergencyMedia em WHERE em.alert = :alert AND em.uploadStatus = 'COMPLETED'")
    long countCompletedMedia(@Param("alert") EmergencyAlert alert);

    @Query("SELECT em FROM EmergencyMedia em WHERE em.uploadedAt < :beforeDate")
    List<EmergencyMedia> findOldMedia(@Param("beforeDate") LocalDateTime beforeDate);

    boolean existsByAlertAndMediaType(EmergencyAlert alert, EmergencyMedia.MediaType mediaType);
}
