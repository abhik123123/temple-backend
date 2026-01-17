
package com.temple.repository;
import com.temple.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.eventDate < :currentDate")
    List<Event> findPastEvents(LocalDate currentDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Event e WHERE e.eventDate < :currentDate")
    int deletePastEvents(LocalDate currentDate);
}
