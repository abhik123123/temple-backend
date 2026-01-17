package com.temple.service;

import com.temple.entity.Staff;
import com.temple.entity.Photo;
import com.temple.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class StaffService {

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private IntegratedPhotoService integratedPhotoService;

    public List<Staff> getAllStaff() {
        return staffRepository.findAllOrderByPosition();
    }

    public List<Staff> getStaffByDepartment(String department) {
        if (department == null || department.isEmpty() || "All".equalsIgnoreCase(department)) {
            return getAllStaff();
        }
        return staffRepository.findByDepartment(department);
    }

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id).orElse(null);
    }

    /**
     * Save staff with optional photo upload
     */
    public Staff saveStaff(Staff staff, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("STAFF_CREATE_WITH_PHOTO | staffName={} | hasPhoto={}",
                   staff.getFullName(), photoFile != null && !photoFile.isEmpty());

        // Save staff first
        Staff savedStaff = staffRepository.save(staff);
        logger.debug("STAFF_SAVED | staffId={}", savedStaff.getId());

        // Save photo if provided
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                Photo photo = integratedPhotoService.savePhotoForEntity(
                        "staff",
                        savedStaff.getId(),
                        photoFile,
                        uploadedBy,
                        "Staff profile photo - " + staff.getFullName()
                );

                if (photo != null) {
                    savedStaff.setProfileImageUrl(photo.getPhotoUrl());
                    staffRepository.save(savedStaff);
                    logger.info("STAFF_PHOTO_SAVED | staffId={} | photoId={}", savedStaff.getId(), photo.getId());
                }
            } catch (IOException e) {
                logger.error("STAFF_PHOTO_SAVE_FAILED | error={}", e.getMessage());
                // Continue without photo - don't fail the staff creation
            }
        }

        return savedStaff;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public Staff saveStaff(Staff staff) {
        try {
            return saveStaff(staff, null, "system");
        } catch (IOException e) {
            logger.error("STAFF_SAVE_ERROR | error={}", e.getMessage());
            return staffRepository.save(staff);
        }
    }

    /**
     * Update staff with optional photo upload
     */
    public Staff updateStaff(Long id, Staff staff, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("STAFF_UPDATE_WITH_PHOTO | staffId={} | hasPhoto={}",
                   id, photoFile != null && !photoFile.isEmpty());

        if (staffRepository.existsById(id)) {
            staff.setId(id);
            Staff updatedStaff = staffRepository.save(staff);
            logger.debug("STAFF_UPDATED | staffId={}", id);

            // Update photo if provided
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    // Delete old photo
                    integratedPhotoService.deletePhotoForEntity(id);

                    // Save new photo
                    Photo photo = integratedPhotoService.savePhotoForEntity(
                            "staff",
                            id,
                            photoFile,
                            uploadedBy,
                            "Staff profile photo - " + staff.getFullName()
                    );

                    if (photo != null) {
                        updatedStaff.setProfileImageUrl(photo.getPhotoUrl());
                        staffRepository.save(updatedStaff);
                        logger.info("STAFF_PHOTO_UPDATED | staffId={} | photoId={}", id, photo.getId());
                    }
                } catch (IOException e) {
                    logger.error("STAFF_PHOTO_UPDATE_FAILED | error={}", e.getMessage());
                    // Continue without photo update
                }
            }

            return updatedStaff;
        }
        return null;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public Staff updateStaff(Long id, Staff staff) {
        try {
            return updateStaff(id, staff, null, "system");
        } catch (IOException e) {
            logger.error("STAFF_UPDATE_ERROR | error={}", e.getMessage());
            if (staffRepository.existsById(id)) {
                staff.setId(id);
                return staffRepository.save(staff);
            }
            return null;
        }
    }

    /**
     * Delete staff (also deletes associated photos)
     */
    public void deleteStaff(Long id) {
        logger.info("STAFF_DELETE | staffId={}", id);
        // Delete photos
        integratedPhotoService.deletePhotoForEntity(id);
        // Delete staff
        staffRepository.deleteById(id);
    }
}

