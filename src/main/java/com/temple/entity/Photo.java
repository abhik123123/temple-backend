package com.temple.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String photoName;

    private String photoType; // events, staff, donors, board_members

    private Long relatedId; // ID of the related entity (event, staff, donor, board member)

    @Column(columnDefinition = "TEXT")
    private String photoData; // Base64 encoded image data or file path

    private String photoUrl; // URL to access the photo

    private String uploadedBy; // Admin username who uploaded

    private LocalDateTime uploadedDate;

    private String status; // Active, Deleted

    private String description;

    private Long fileSize; // Size in bytes

    @Column(length = 100)
    private String mimeType; // image/jpeg, image/png, etc.
}

