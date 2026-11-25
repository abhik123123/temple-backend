package com.temple.repository;

import com.temple.entity.Donor;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    @Query("SELECT d FROM Donor d ORDER BY d.donationDate DESC NULLS LAST, d.id DESC")
    List<Donor> findAllNewestFirst();

    @Query("SELECT SUM(d.donationAmount) FROM Donor d")
    BigDecimal sumDonations();

    @Query("SELECT COUNT(DISTINCT LOWER(d.name)) FROM Donor d")
    long countDistinctDonors();
}
