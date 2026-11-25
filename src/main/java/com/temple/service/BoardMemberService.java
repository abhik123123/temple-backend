package com.temple.service;

import com.temple.entity.BoardMember;
import com.temple.entity.Photo;
import com.temple.exception.NotFoundException;
import com.temple.repository.BoardMemberRepository;
import com.temple.repository.BoardMemberServiceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class BoardMemberService {

    static final String PHOTO_TYPE = "board_members";

    private final BoardMemberRepository repository;
    private final BoardMemberServiceRepository servicePeriods;
    private final PhotoService photoService;

    public BoardMemberService(BoardMemberRepository repository, BoardMemberServiceRepository servicePeriods,
                              PhotoService photoService) {
        this.repository = repository;
        this.servicePeriods = servicePeriods;
        this.photoService = photoService;
    }

    @Transactional(readOnly = true)
    public List<BoardMember> getAllBoardMembers() {
        return repository.findAllByOrderByPositionAscFullNameAsc();
    }

    @Transactional(readOnly = true)
    public List<BoardMember> getMembersByDepartment(String department) {
        if (department == null || department.isBlank() || "all".equalsIgnoreCase(department.trim())) {
            return getAllBoardMembers();
        }
        return repository.findByDepartmentIgnoreCaseOrderByPositionAscFullNameAsc(department.trim());
    }

    @Transactional(readOnly = true)
    public BoardMember getMemberById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Board member", id));
    }

    public BoardMember saveMember(BoardMember person, MultipartFile photoFile, String uploadedBy) {
        person.setId(null);
        BoardMember saved = repository.save(person);
        attachPhoto(saved, photoFile, uploadedBy);
        return saved;
    }

    /** Copies editable fields onto the stored record; the photo is kept unless a new one is uploaded or a URL is given. */
    public BoardMember updateMember(Long id, BoardMember changes, MultipartFile photoFile, String uploadedBy) {
        BoardMember person = getMemberById(id);
        person.setFullName(changes.getFullName());
        person.setPosition(changes.getPosition());
        person.setDepartment(changes.getDepartment());
        person.setPhoneNumber(changes.getPhoneNumber());
        person.setEmail(changes.getEmail());
        person.setBiography(changes.getBiography());
        person.setExperience(changes.getExperience());
        if (changes.getStatus() != null) {
            person.setStatus(changes.getStatus());
        }
        if (changes.getProfileImageUrl() != null && !changes.getProfileImageUrl().isBlank()) {
            person.setProfileImageUrl(changes.getProfileImageUrl());
        }
        attachPhoto(person, photoFile, uploadedBy);
        return person;
    }

    public void deleteMember(Long id) {
        BoardMember person = getMemberById(id);
        servicePeriods.deleteAll(servicePeriods.findByBoardMemberId(id));
        photoService.deleteForEntity(PHOTO_TYPE, id);
        repository.delete(person);
    }

    private void attachPhoto(BoardMember person, MultipartFile photoFile, String uploadedBy) {
        if (photoFile == null || photoFile.isEmpty()) {
            return;
        }
        Photo photo = photoService.replaceForEntity(PHOTO_TYPE, person.getId(), photoFile, uploadedBy,
                "Profile photo - " + person.getFullName());
        person.setProfileImageUrl(photo.getPhotoUrl());
    }
}
