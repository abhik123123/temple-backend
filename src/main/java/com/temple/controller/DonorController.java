package com.temple.controller;

import com.temple.dto.PublicDonation;
import com.temple.entity.Donor;
import com.temple.service.DonorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/donors. The list and statistics are public (donor acknowledgement page)
 * and never include contact details. Full records and writes require ADMIN.
 */
@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping
    public List<PublicDonation> list() {
        return donorService.getPublicDonations();
    }

    @GetMapping("/statistics")
    public DonorService.DonationStatistics statistics() {
        return donorService.statistics();
    }

    /** Admin: full records including contact details. */
    @GetMapping("/admin/all")
    public List<Donor> listFull() {
        return donorService.getAllDonors();
    }

    @GetMapping("/{id}")
    public Donor get(@PathVariable Long id) {
        return donorService.getDonorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Donor create(@Valid @RequestBody Donor donor) {
        return donorService.saveDonor(donor);
    }

    @PutMapping("/{id}")
    public Donor update(@PathVariable Long id, @Valid @RequestBody Donor donor) {
        return donorService.updateDonor(id, donor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        donorService.deleteDonor(id);
    }
}
