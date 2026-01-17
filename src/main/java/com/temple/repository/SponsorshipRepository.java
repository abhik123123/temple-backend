package com.temple.repository;

import com.temple.entity.Sponsorship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {
    List<Sponsorship> findByPurpose(String purpose);
    List<Sponsorship> findByActive(boolean active);
}
