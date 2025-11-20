package com.women.safety.features.smartAlert.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.smartAlert.model.SmartAlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmartAlertSettingsRepository extends JpaRepository<SmartAlertSettings, Long> {

    Optional<SmartAlertSettings> findByUser(AuthUser user);

    boolean existsByUser(AuthUser user);
}