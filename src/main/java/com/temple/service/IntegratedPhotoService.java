package com.temple.service;

import com.temple.entity.Photo;
import com.temple.repository.PhotoRepository;
import com.temple.util.PhotoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Integrated Photo Service for storing photos with entities
 * Used when photos are uploaded along with staff, events, board members, etc.
 */
@Service
public class IntegratedPhotoService {

    private static final Logger logger = LoggerFactory.getLogger(IntegratedPhotoService.class);

    @Autowired
    private PhotoRepository photoRepository;

    /**
     * Save photo for an entity during creation/update
     * Integrated into entity CRUD operations
     */
    public Photo savePhotoForEntity(String entityType, Long entityId, MultipartFile photoFile,
                                    String uploadedBy, String description) throws IOException {

        logger.info("INTEGRATED_PHOTO_SAVE_START | type={} | entityId={} | uploadedBy={}",
                   entityType, entityId, uploadedBy);

        // Validate file
        if (photoFile == null || photoFile.isEmpty()) {
            logger.debug("No photo file provided");
            return null;
        }

        if (!PhotoUtil.isValidImageFile(photoFile)) {
            logger.warn("INVALID_IMAGE_FILE | mimeType={}", photoFile.getContentType());
            throw new IOException("Invalid image file format. Supported formats: " + PhotoUtil.getSupportedFormatsString());
        }

        try {
            // Convert to Base64
            String photoData = PhotoUtil.convertToBase64(photoFile);
            String fileName = photoFile.getOriginalFilename();
            String mimeType = PhotoUtil.getMimeType(photoFile);
            long fileSize = PhotoUtil.getFileSize(photoFile);

            // Create Photo entity
            Photo photo = Photo.builder()
                    .photoName(fileName)
                    .photoType(entityType)
                    .relatedId(entityId)
                    .photoData(photoData)
                    .photoUrl(PhotoUtil.generatePhotoUrl(entityType, entityId, fileName))
                    .uploadedBy(uploadedBy)
                    .uploadedDate(LocalDateTime.now())
                    .status("Active")
                    .description(description != null ? description : "Photo for " + entityType)
                    .fileSize(fileSize)
                    .mimeType(mimeType)
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            logger.info("INTEGRATED_PHOTO_SAVE_SUCCESS | photoId={} | size={}bytes",
                       savedPhoto.getId(), fileSize);
            return savedPhoto;

        } catch (IOException e) {
            logger.error("INTEGRATED_PHOTO_SAVE_ERROR | error={}", e.getMessage(), e);
            throw new IOException("Failed to save photo: " + e.getMessage());
        }
    }

    /**
     * Update photo for an entity
     */
    public Photo updatePhotoForEntity(Long entityId, MultipartFile newPhotoFile,
                                      String uploadedBy) throws IOException {

        logger.info("INTEGRATED_PHOTO_UPDATE_START | entityId={}", entityId);

        // Get existing photo
        List<Photo> existingPhotos = photoRepository.findByRelatedId(entityId);

        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            // Mark old photo as deleted
            for (Photo oldPhoto : existingPhotos) {
                oldPhoto.setStatus("Deleted");
                photoRepository.save(oldPhoto);
                logger.debug("Old photo marked as deleted | photoId={}", oldPhoto.getId());
            }
        }

        // Save new photo
        return savePhotoForEntity("profile", entityId, newPhotoFile, uploadedBy, "Updated profile photo");
    }

    /**
     * Get photo for entity
     */
    public Photo getPhotoForEntity(Long entityId) {
        logger.debug("INTEGRATED_PHOTO_FETCH | entityId={}", entityId);

        List<Photo> photos = photoRepository.findByRelatedId(entityId);
        if (photos != null && !photos.isEmpty()) {
            return photos.stream()
                    .filter(p -> "Active".equals(p.getStatus()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Get photos by entity type
     */
    public List<Photo> getPhotosByEntityType(String entityType) {
        logger.debug("INTEGRATED_PHOTO_FETCH_BY_TYPE | type={}", entityType);
        return photoRepository.findByPhotoTypeAndStatus(entityType, "Active");
    }

    /**
     * Delete photo for entity
     */
    public void deletePhotoForEntity(Long entityId) {
        logger.info("INTEGRATED_PHOTO_DELETE | entityId={}", entityId);

        List<Photo> photos = photoRepository.findByRelatedId(entityId);
        if (photos != null && !photos.isEmpty()) {
            for (Photo photo : photos) {
                photo.setStatus("Deleted");
                photoRepository.save(photo);
            }
            logger.info("INTEGRATED_PHOTO_DELETE_SUCCESS | entityId={} | count={}", entityId, photos.size());
        }
    }

    /**
     * Get all photos
     */
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    /**
     * Get all active photos
     */
    public List<Photo> getAllActivePhotos() {
        return photoRepository.findByStatus("Active");
    }
}

