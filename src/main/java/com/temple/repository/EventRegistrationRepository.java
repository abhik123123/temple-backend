package com.temple.repository;

import com.temple.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByEventId(Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(r.totalMembers), 0) FROM EventRegistration r WHERE r.event.id = :eventId")
    Integer getTotalMembersByEventId(@Param("eventId") Long eventId);
}

