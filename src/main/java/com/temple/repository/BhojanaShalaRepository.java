package com.temple.repository;

import com.temple.entity.BhojanaShala;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BhojanaShalaRepository extends JpaRepository<BhojanaShala, Long> {
    List<BhojanaShala> findByActive(boolean active);
}
