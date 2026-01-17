package com.temple.repository;

import com.temple.entity.TempleTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TempleTimingRepository extends JpaRepository<TempleTiming, Long> {
    List<TempleTiming> findByDayOfWeek(String dayOfWeek);
}
