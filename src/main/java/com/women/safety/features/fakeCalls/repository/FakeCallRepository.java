package com.women.safety.features.fakeCalls.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.fakeCalls.model.FakeCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FakeCallRepository extends JpaRepository<FakeCall, Long> {

    List<FakeCall> findByUserOrderByCreatedAtDesc(AuthUser user);

    List<FakeCall> findByUserAndIsPresetTrueOrderByTriggerCountDesc(AuthUser user);

    @Query("SELECT fc FROM FakeCall fc WHERE fc.user = :user AND fc.isPreset = true ORDER BY fc.lastTriggeredAt DESC LIMIT 1")
    Optional<FakeCall> findMostRecentlyUsedPreset(@Param("user") AuthUser user);

    @Query("SELECT fc FROM FakeCall fc WHERE fc.user = :user ORDER BY fc.triggerCount DESC LIMIT 1")
    Optional<FakeCall> findMostUsedPreset(@Param("user") AuthUser user);

    @Query("SELECT COUNT(fc) FROM FakeCall fc WHERE fc.user = :user AND fc.isPreset = true")
    long countPresetsForUser(@Param("user") AuthUser user);
}
