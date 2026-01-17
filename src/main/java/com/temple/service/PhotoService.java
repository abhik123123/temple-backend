package com.temple.service;

import com.temple.entity.Photo;
import com.temple.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Base64;

@Service
public class PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

    @Autowired
    private PhotoRepository photoRepository;

    /**
     * Upload photo for an entity (events, staff, donors, board members)
     */
    public Photo uploadPhoto(String photoType, Long relatedId, MultipartFile file, String uploadedBy, String description) throws IOException {
        logger.info("PHOTO_UPLOAD_START | type={} | relatedId={} | uploadedBy={}", photoType, relatedId, uploadedBy);

        try {
            String photoName = file.getOriginalFilename();
            String mimeType = file.getContentType();
            long fileSize = file.getSize();

            // Convert file to Base64
            String photoData = Base64.getEncoder().encodeToString(file.getBytes());

            Photo photo = Photo.builder()
                    .photoName(photoName)
                    .photoType(photoType)
                    .relatedId(relatedId)
                    .photoData(photoData)
                    .photoUrl("/api/photos/" + relatedId + "/" + photoType + "/" + photoName)
                    .uploadedBy(uploadedBy)
                    .uploadedDate(LocalDateTime.now())
                    .status("Active")
                    .description(description)
                    .fileSize(fileSize)
                    .mimeType(mimeType)
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            logger.info("PHOTO_UPLOAD_SUCCESS | photoId={} | fileSize={}bytes", savedPhoto.getId(), fileSize);
            return savedPhoto;

        } catch (IOException e) {
            logger.error("PHOTO_UPLOAD_ERROR | error={}", e.getMessage(), e);
            throw new IOException("Failed to upload photo: " + e.getMessage());
        }
    }

    /**
     * Get all photos for a specific type
     */
    public List<Photo> getPhotosByType(String photoType) {
        logger.debug("PHOTO_FETCH_START | type={}", photoType);
        return photoRepository.findByPhotoTypeAndStatus(photoType, "Active");
    }

    /**
     * Get photos for a specific entity
     */
    public List<Photo> getPhotosByRelatedId(Long relatedId) {
        logger.debug("PHOTO_FETCH_START | relatedId={}", relatedId);
        return photoRepository.findByRelatedId(relatedId);
    }

    /**
     * Get specific photo for entity
     */
    public List<Photo> getPhotosByTypeAndRelatedId(String photoType, Long relatedId) {
        logger.debug("PHOTO_FETCH_START | type={} | relatedId={}", photoType, relatedId);
        return photoRepository.findByPhotoTypeAndRelatedId(photoType, relatedId);
    }

    /**
     * Delete/deactivate photo
     */
    public void deletePhoto(Long photoId) {
        logger.info("PHOTO_DELETE_START | photoId={}", photoId);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        photo.setStatus("Deleted");
        photoRepository.save(photo);

        logger.info("PHOTO_DELETE_SUCCESS | photoId={}", photoId);
    }

    /**
     * Get photo by ID
     */
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + photoId));
    }

    /**
     * Get all photos
     */
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    /**
     * Update photo description
     */
    public Photo updatePhotoDescription(Long photoId, String description) {
        Photo photo = getPhotoById(photoId);
        photo.setDescription(description);
        return photoRepository.save(photo);
    }
}

