package com.temple.service;

import com.temple.entity.Staff;
import com.temple.entity.Photo;
import com.temple.exception.NotFoundException;
import com.temple.repository.StaffRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class StaffService {

    static final String PHOTO_TYPE = "staff";

    private final StaffRepository repository;
    private final PhotoService photoService;

    public StaffService(StaffRepository repository, PhotoService photoService) {
        this.repository = repository;
        this.photoService = photoService;
    }

    @Transactional(readOnly = true)
    public List<Staff> getAllStaff() {
        return repository.findAllByOrderByPositionAscFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByDepartment(String department) {
        if (department == null || department.isBlank() || "all".equalsIgnoreCase(department.trim())) {
            return getAllStaff();
        }
        return repository.findByDepartmentIgnoreCaseOrderByPositionAscFullNameAsc(department.trim());
    }

    @Transactional(readOnly = true)
    public Staff getStaffById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Staff member", id));
    }

    public Staff saveStaff(Staff person, MultipartFile photoFile, String uploadedBy) {
        person.setId(null);
        Staff saved = repository.save(person);
        attachPhoto(saved, photoFile, uploadedBy);
        return saved;
    }

    /** Copies editable fields onto the stored record; the photo is kept unless a new one is uploaded or a URL is given. */
    public Staff updateStaff(Long id, Staff changes, MultipartFile photoFile, String uploadedBy) {
        Staff person = getStaffById(id);
        person.setFullName(changes.getFullName());
        person.setPosition(changes.getPosition());
        person.setDepartment(changes.getDepartment());
        person.setPhoneNumber(changes.getPhoneNumber());
        person.setEmail(changes.getEmail());
        person.setBiography(changes.getBiography());
        person.setExperience(changes.getExperience());
        if (changes.getStatus() != null) {
            person.setStatus(changes.getStatus());
        }
        if (changes.getProfileImageUrl() != null && !changes.getProfileImageUrl().isBlank()) {
            person.setProfileImageUrl(changes.getProfileImageUrl());
        }
        attachPhoto(person, photoFile, uploadedBy);
        return person;
    }

    public void deleteStaff(Long id) {
        Staff person = getStaffById(id);
        photoService.deleteForEntity(PHOTO_TYPE, id);
        repository.delete(person);
    }

    private void attachPhoto(Staff person, MultipartFile photoFile, String uploadedBy) {
        if (photoFile == null || photoFile.isEmpty()) {
            return;
        }
        Photo photo = photoService.replaceForEntity(PHOTO_TYPE, person.getId(), photoFile, uploadedBy,
                "Profile photo - " + person.getFullName());
        person.setProfileImageUrl(photo.getPhotoUrl());
    }
}
