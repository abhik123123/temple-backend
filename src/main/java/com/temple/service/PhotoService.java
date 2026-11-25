package com.temple.service;

import com.temple.config.AppProperties;
import com.temple.entity.Photo;
import com.temple.exception.BadRequestException;
import com.temple.exception.NotFoundException;
import com.temple.repository.PhotoRepository;
import com.temple.util.PhotoUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Photos are stored base64-encoded in the {@code photos} table (simple to back
 * up together with the database) and served by {@code GET /api/photos/{id}/content}.
 * Photos are always scoped by (type, relatedId): the same numeric id is used
 * by different entity types.
 */
@Service
@Transactional
public class PhotoService {

    public static final String ACTIVE = "Active";
    public static final String DELETED = "Deleted";

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
    private static final Pattern SAFE_TYPE = Pattern.compile("^[a-z0-9_-]{1,40}$");

    private final PhotoRepository photoRepository;
    private final long maxBytes;

    public PhotoService(PhotoRepository photoRepository, AppProperties properties) {
        this.photoRepository = photoRepository;
        this.maxBytes = properties.photos().maxBytes();
    }

    public record PhotoContent(byte[] bytes, String mimeType) {
    }

    /** Validates and stores an image, returning the saved photo with its public URL set. */
    public Photo upload(String photoType, Long relatedId, MultipartFile file, String uploadedBy, String description) {
        String type = normalizeType(photoType);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Photo file is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Photo is too large (max " + (maxBytes / (1024 * 1024)) + " MB)");
        }
        String mimeType = PhotoUtil.detectImageMimeType(file).orElseThrow(() ->
                new BadRequestException("Unsupported image format. Allowed: " + PhotoUtil.getSupportedFormatsString()));
        String data;
        try {
            data = Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read upload", e);
        }
        Photo photo = photoRepository.save(Photo.builder()
                .photoName(PhotoUtil.safeFileName(file.getOriginalFilename()))
                .photoType(type)
                .relatedId(relatedId)
                .photoData(data)
                .uploadedBy(uploadedBy)
                .uploadedDate(LocalDateTime.now())
                .status(ACTIVE)
                .description(description == null ? null : truncate(description, 255))
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .build());
        photo.setPhotoUrl(contentUrl(photo.getId()));
        log.info("PHOTO_SAVED | id={} | type={} | relatedId={} | bytes={}", photo.getId(), type, relatedId, file.getSize());
        return photo;
    }

    /** Replaces the photo of one entity: previous photos of the same (type, id) are marked deleted. */
    public Photo replaceForEntity(String photoType, Long relatedId, MultipartFile file, String uploadedBy, String description) {
        Photo saved = upload(photoType, relatedId, file, uploadedBy, description);
        photoRepository.findByPhotoTypeAndRelatedId(normalizeType(photoType), relatedId).stream()
                .filter(p -> !p.getId().equals(saved.getId()))
                .forEach(p -> p.setStatus(DELETED));
        return saved;
    }

    public void deleteForEntity(String photoType, Long relatedId) {
        photoRepository.findByPhotoTypeAndRelatedId(normalizeType(photoType), relatedId)
                .forEach(p -> p.setStatus(DELETED));
    }

    @Transactional(readOnly = true)
    public PhotoContent content(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .filter(p -> ACTIVE.equals(p.getStatus()) && p.getPhotoData() != null)
                .orElseThrow(() -> new NotFoundException("Photo", photoId));
        return new PhotoContent(Base64.getDecoder().decode(photo.getPhotoData()),
                photo.getMimeType() == null ? "application/octet-stream" : photo.getMimeType());
    }

    @Transactional(readOnly = true)
    public List<Photo> byType(String photoType) {
        return photoRepository.findByPhotoTypeAndStatus(normalizeType(photoType), ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Photo> byTypeAndRelatedId(String photoType, Long relatedId) {
        return photoRepository.findByPhotoTypeAndRelatedId(normalizeType(photoType), relatedId).stream()
                .filter(p -> ACTIVE.equals(p.getStatus())).toList();
    }

    @Transactional(readOnly = true)
    public List<Photo> all() {
        return photoRepository.findAll();
    }

    public void delete(Long photoId) {
        Photo photo = photoRepository.findById(photoId).orElseThrow(() -> new NotFoundException("Photo", photoId));
        photo.setStatus(DELETED);
    }

    public Photo updateDescription(Long photoId, String description) {
        Photo photo = photoRepository.findById(photoId).orElseThrow(() -> new NotFoundException("Photo", photoId));
        photo.setDescription(description == null ? null : truncate(description, 255));
        return photo;
    }

    public static String contentUrl(Long photoId) {
        return "/api/photos/" + photoId + "/content";
    }

    private static String normalizeType(String photoType) {
        String type = photoType == null ? "" : photoType.trim().toLowerCase(Locale.ROOT);
        if (!SAFE_TYPE.matcher(type).matches()) {
            throw new BadRequestException("Invalid photo type");
        }
        return type;
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
