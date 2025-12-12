package com.women.safety.features.fakeCalls.repository;

import com.women.safety.features.authentication.model.AuthUser;
import com.women.safety.features.fakeCalls.model.FakeCall;
import com.women.safety.features.fakeCalls.model.FakeCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FakeCallLogRepository extends JpaRepository<FakeCallLog, Long> {

    List<FakeCallLog> findByUserOrderByCallStartedAtDesc(AuthUser user);

    @Query("SELECT fcl FROM FakeCallLog fcl WHERE fcl.user = :user AND fcl.callStartedAt >= :fromDate ORDER BY fcl.callStartedAt DESC")
    List<FakeCallLog> findRecentLogs(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(fcl) FROM FakeCallLog fcl WHERE fcl.user = :user")
    long countByUser(@Param("user") AuthUser user);

    @Query("SELECT COUNT(fcl) FROM FakeCallLog fcl WHERE fcl.user = :user AND fcl.callStartedAt >= :fromDate")
    long countRecentCalls(@Param("user") AuthUser user, @Param("fromDate") LocalDateTime fromDate);

    void deleteByFakeCall(FakeCall fakeCall);

    @Query("SELECT fcl FROM FakeCallLog fcl WHERE fcl.user = :user AND fcl.triggerMethod = :method ORDER BY fcl.callStartedAt DESC")
    List<FakeCallLog> findByTriggerMethod(@Param("user") AuthUser user, @Param("method") FakeCallLog.TriggerMethod method);
}
