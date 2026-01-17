package com.temple.controller;

import com.temple.entity.Photo;
import com.temple.security.AdminOnly;
import com.temple.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Base64;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    @Autowired
    private PhotoService photoService;

    /**
     * Upload photo (Admin only)
     */
    @AdminOnly
    @PostMapping("/upload/{photoType}/{relatedId}")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable String photoType,
            @PathVariable Long relatedId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {

        logger.info("PHOTO_UPLOAD_REQUEST | type={} | relatedId={} | user={}", photoType, relatedId, authentication.getName());

        try {
            Photo photo = photoService.uploadPhoto(photoType, relatedId, file, authentication.getName(), description);
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            logger.error("PHOTO_UPLOAD_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(400).body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Get all photos by type
     */
    @GetMapping("/type/{photoType}")
    public ResponseEntity<List<Photo>> getPhotosByType(@PathVariable String photoType) {
        logger.info("PHOTO_FETCH_REQUEST | type={}", photoType);
        List<Photo> photos = photoService.getPhotosByType(photoType);
        return ResponseEntity.ok(photos);
    }

    /**
     * Get photos for specific entity
     */
    @GetMapping("/entity/{relatedId}")
    public ResponseEntity<List<Photo>> getPhotosByEntity(@PathVariable Long relatedId) {
        logger.info("PHOTO_FETCH_REQUEST | relatedId={}", relatedId);
        List<Photo> photos = photoService.getPhotosByRelatedId(relatedId);
        return ResponseEntity.ok(photos);
    }

    /**
     * Get specific photos for entity type
     */
    @GetMapping("/{photoType}/{relatedId}")
    public ResponseEntity<List<Photo>> getPhotosByTypeAndEntity(
            @PathVariable String photoType,
            @PathVariable Long relatedId) {
        logger.info("PHOTO_FETCH_REQUEST | type={} | relatedId={}", photoType, relatedId);
        List<Photo> photos = photoService.getPhotosByTypeAndRelatedId(photoType, relatedId);
        return ResponseEntity.ok(photos);
    }

    /**
     * Download photo (get photo data)
     */
    @GetMapping("/download/{photoId}")
    public ResponseEntity<byte[]> downloadPhoto(@PathVariable Long photoId) {
        logger.info("PHOTO_DOWNLOAD_REQUEST | photoId={}", photoId);

        Photo photo = photoService.getPhotoById(photoId);
        byte[] photoData = Base64.getDecoder().decode(photo.getPhotoData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photo.getPhotoName() + "\"")
                .contentType(MediaType.parseMediaType(photo.getMimeType()))
                .body(photoData);
    }

    /**
     * Get all photos (Admin only)
     */
    @AdminOnly
    @GetMapping("/all")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        logger.info("PHOTO_FETCH_ALL_REQUEST");
        List<Photo> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }

    /**
     * Delete photo (Admin only)
     */
    @AdminOnly
    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long photoId) {
        logger.info("PHOTO_DELETE_REQUEST | photoId={}", photoId);

        try {
            photoService.deletePhoto(photoId);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (Exception e) {
            logger.error("PHOTO_DELETE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(404).body("Photo not found");
        }
    }

    /**
     * Update photo description (Admin only)
     */
    @AdminOnly
    @PutMapping("/{photoId}/description")
    public ResponseEntity<?> updatePhotoDescription(
            @PathVariable Long photoId,
            @RequestParam String description) {

        logger.info("PHOTO_UPDATE_DESCRIPTION_REQUEST | photoId={}", photoId);

        try {
            Photo updatedPhoto = photoService.updatePhotoDescription(photoId, description);
            return ResponseEntity.ok(updatedPhoto);
        } catch (Exception e) {
            logger.error("PHOTO_UPDATE_FAILED | error={}", e.getMessage());
            return ResponseEntity.status(404).body("Photo not found");
        }
    }
}

