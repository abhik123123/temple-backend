package com.temple.service;

import com.temple.dto.PublicDonation;
import com.temple.entity.Donor;
import com.temple.exception.NotFoundException;
import com.temple.repository.DonorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DonorService {

    public record DonationStatistics(long totalDonors, long donationCount,
                                     BigDecimal totalDonations, BigDecimal averageDonation) {
    }

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    @Transactional(readOnly = true)
    public List<PublicDonation> getPublicDonations() {
        return donorRepository.findAllNewestFirst().stream().map(PublicDonation::from).toList();
    }

    @Transactional(readOnly = true)
    public List<Donor> getAllDonors() {
        return donorRepository.findAllNewestFirst();
    }

    @Transactional(readOnly = true)
    public Donor getDonorById(Long id) {
        return donorRepository.findById(id).orElseThrow(() -> new NotFoundException("Donation", id));
    }

    @Transactional(readOnly = true)
    public DonationStatistics statistics() {
        long count = donorRepository.count();
        BigDecimal total = donorRepository.sumDonations();
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        BigDecimal average = count == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        return new DonationStatistics(donorRepository.countDistinctDonors(), count, total, average);
    }

    public Donor saveDonor(Donor donor) {
        donor.setId(null);
        if (donor.getDonationDate() == null) {
            donor.setDonationDate(LocalDate.now());
        }
        return donorRepository.save(donor);
    }

    public Donor updateDonor(Long id, Donor changes) {
        getDonorById(id);
        changes.setId(id);
        if (changes.getDonationDate() == null) {
            changes.setDonationDate(LocalDate.now());
        }
        return donorRepository.save(changes);
    }

    public void deleteDonor(Long id) {
        donorRepository.delete(getDonorById(id));
    }
}
