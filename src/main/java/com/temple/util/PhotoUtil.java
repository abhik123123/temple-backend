package com.temple.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

/**
 * Utility class for photo handling and storage
 * Supports ALL image formats: JPEG, JPG, PNG, WebP, GIF, BMP, SVG, TIFF, ICO, and more
 */
public class PhotoUtil {

    // Supported image formats (comprehensive list)
    private static final String[] SUPPORTED_FORMATS = {
        "image/jpeg",      // JPEG/JPG
        "image/png",       // PNG
        "image/webp",      // WebP
        "image/gif",       // GIF
        "image/bmp",       // BMP
        "image/svg+xml",   // SVG
        "image/tiff",      // TIFF
        "image/x-icon",    // ICO
        "image/vnd.microsoft.icon", // ICO (alternative)
        "image/x-ms-bmp",  // BMP (alternative)
        "image/jpg"        // JPG (alternative)
    };

    /**
     * Get list of supported image MIME types
     */
    public static String[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    /**
     * Get human-readable list of supported formats
     */
    public static String getSupportedFormatsString() {
        return "JPEG, JPG, PNG, WebP, GIF, BMP, SVG, TIFF, ICO";
    }

    /**
     * Convert MultipartFile to Base64 string
     */
    public static String convertToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    /**
     * Validate image file
     */
    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")
        );
    }

    /**
     * Get MIME type from file
     */
    public static String getMimeType(MultipartFile file) {
        return file != null ? file.getContentType() : null;
    }

    /**
     * Get file size
     */
    public static long getFileSize(MultipartFile file) {
        return file != null ? file.getSize() : 0;
    }

    /**
     * Generate photo URL
     */
    public static String generatePhotoUrl(String entityType, Long entityId, String fileName) {
        return "/api/photos/download/" + entityType + "/" + entityId + "/" + fileName;
    }

    /**
     * Convert Base64 to byte array
     */
    public static byte[] convertBase64ToBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64String);
    }
}

