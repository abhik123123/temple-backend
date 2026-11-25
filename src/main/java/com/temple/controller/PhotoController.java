package com.temple.controller;

import com.temple.entity.Photo;
import com.temple.service.PhotoService;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Base path /api/photos. Image bytes are served by GET /{id}/content; JSON listings never include image data. */
@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload/{photoType}/{relatedId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Photo upload(@PathVariable String photoType,
                        @PathVariable Long relatedId,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "description", required = false) @Size(max = 255) String description,
                        Authentication authentication) {
        return photoService.upload(photoType, relatedId, file, authentication.getName(), description);
    }

    /** Public: the image itself. IDs are immutable, so it can be cached for a long time. */
    @GetMapping("/{photoId}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long photoId) {
        PhotoService.PhotoContent content = photoService.content(photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.mimeType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .body(content.bytes());
    }

    /** Kept for older links: same as /{photoId}/content. */
    @GetMapping("/download/{photoId}")
    public ResponseEntity<byte[]> download(@PathVariable Long photoId) {
        return content(photoId);
    }

    @GetMapping("/type/{photoType}")
    public List<Photo> byType(@PathVariable String photoType) {
        return photoService.byType(photoType);
    }

    @GetMapping("/{photoType}/{relatedId}")
    public List<Photo> byTypeAndEntity(@PathVariable String photoType, @PathVariable Long relatedId) {
        return photoService.byTypeAndRelatedId(photoType, relatedId);
    }

    @GetMapping("/all")
    public List<Photo> all() {
        return photoService.all();
    }

    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long photoId) {
        photoService.delete(photoId);
    }

    @PutMapping("/{photoId}/description")
    public Photo updateDescription(@PathVariable Long photoId,
                                   @RequestParam @Size(max = 255) String description) {
        return photoService.updateDescription(photoId, description);
    }
}
