package com.temple.repository;

import com.temple.entity.EventRegistration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByEventId(Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status <> 'CANCELLED'")
    Long countActiveByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(r.totalMembers), 0L) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status <> 'CANCELLED'")
    Long totalActiveMembersByEventId(@Param("eventId") Long eventId);
}
