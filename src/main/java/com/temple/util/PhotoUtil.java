package com.temple.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image validation. The type is decided from the file's magic bytes, never
 * from the client-supplied Content-Type or file name, so HTML/SVG/scripts
 * disguised as images are rejected (they would otherwise be served back from
 * our origin and executed in visitors' browsers).
 */
public final class PhotoUtil {

    private PhotoUtil() {
    }

    public static String getSupportedFormatsString() {
        return "JPEG, PNG, WebP, GIF";
    }

    /** @return the real MIME type (image/jpeg, image/png, image/webp, image/gif) or empty if not a supported image */
    public static Optional<String> detectImageMimeType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        byte[] h = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(h, 0, h.length);
        } catch (IOException e) {
            return Optional.empty();
        }
        if (read >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return Optional.of("image/jpeg");
        }
        if (read >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) {
            return Optional.of("image/png");
        }
        if (read >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return Optional.of("image/webp");
        }
        if (read >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8'
                && (h[4] == '7' || h[4] == '9') && h[5] == 'a') {
            return Optional.of("image/gif");
        }
        return Optional.empty();
    }

    /** Strips path components and unsafe characters from an uploaded file name (used for display only). */
    public static String safeFileName(String original) {
        if (original == null || original.isBlank()) {
            return "photo";
        }
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^A-Za-z0-9._ -]", "_");
        return name.length() > 100 ? name.substring(name.length() - 100) : name;
    }
}
