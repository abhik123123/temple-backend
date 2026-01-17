package com.temple.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.temple.entity.Donor;
import com.temple.repository.DonorRepository;
import java.util.List;

@Service
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id).orElse(null);
    }

    public Donor saveDonor(Donor donor) {
        return donorRepository.save(donor);
    }

    public void deleteDonor(Long id) {
        donorRepository.deleteById(id);
    }

    public Donor updateDonor(Long id, Donor donor) {
        if (donorRepository.existsById(id)) {
            donor.setId(id);
            return donorRepository.save(donor);
        }
        return null;
    }
}

