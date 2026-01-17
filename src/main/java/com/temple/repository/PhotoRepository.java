package com.temple.repository;

import com.temple.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByPhotoType(String photoType);
    List<Photo> findByRelatedId(Long relatedId);
    List<Photo> findByPhotoTypeAndRelatedId(String photoType, Long relatedId);
    List<Photo> findByStatus(String status);
    List<Photo> findByPhotoTypeAndStatus(String photoType, String status);
}

