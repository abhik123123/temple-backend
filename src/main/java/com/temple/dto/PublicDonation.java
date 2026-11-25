package com.temple.dto;

import com.temple.entity.Donor;
import java.math.BigDecimal;
import java.time.LocalDate;

/** What the public donations page may show: no email, phone or address. */
public record PublicDonation(Long id, String donorName, String donationType, BigDecimal amount,
                             LocalDate donationDate, String inMemoryOf, String donationReason) {

    public static PublicDonation from(Donor d) {
        return new PublicDonation(d.getId(), d.getName(), d.getDonationType(), d.getDonationAmount(),
                d.getDonationDate(), d.getInMemoryOf(), d.getDonationReason());
    }
}
