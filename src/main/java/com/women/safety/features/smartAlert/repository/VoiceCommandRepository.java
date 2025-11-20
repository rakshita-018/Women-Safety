package com.women.safety.features.smartAlert.repository;


import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.smartAlert.model.VoiceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoiceCommandRepository extends JpaRepository<VoiceCommand, Long> {

    List<VoiceCommand> findByUserOrderByTimestampDesc(AuthUser user);

    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user AND vc.timestamp >= :fromDate ORDER BY vc.timestamp DESC")
    List<VoiceCommand> findRecentCommands(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user AND vc.commandType = :commandType ORDER BY vc.timestamp DESC")
    List<VoiceCommand> findByCommandType(@Param("user") AuthUser user, @Param("commandType") VoiceCommand.CommandType commandType);

    @Query("SELECT vc FROM VoiceCommand vc WHERE vc.user = :user AND vc.alertTriggered = true ORDER BY vc.timestamp DESC")
    List<VoiceCommand> findAlertTriggeringCommands(@Param("user") AuthUser user);
}
