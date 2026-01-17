package com.temple.service;

import com.temple.entity.BoardMember;
import com.temple.entity.Photo;
import com.temple.repository.BoardMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class BoardMemberService {

    private static final Logger logger = LoggerFactory.getLogger(BoardMemberService.class);

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private IntegratedPhotoService integratedPhotoService;

    public List<BoardMember> getAllMembers() {
        return boardMemberRepository.findAllOrderByPosition();
    }

    public List<BoardMember> getMembersByDepartment(String department) {
        if (department == null || department.isEmpty() || "All".equalsIgnoreCase(department)) {
            return getAllMembers();
        }
        return boardMemberRepository.findByDepartment(department);
    }

    public BoardMember getMemberById(Long id) {
        return boardMemberRepository.findById(id).orElse(null);
    }

    /**
     * Save board member with optional photo upload
     */
    public BoardMember saveMember(BoardMember member, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("BOARD_MEMBER_CREATE_WITH_PHOTO | memberName={} | hasPhoto={}",
                   member.getFullName(), photoFile != null && !photoFile.isEmpty());

        // Save member first
        BoardMember savedMember = boardMemberRepository.save(member);
        logger.debug("BOARD_MEMBER_SAVED | memberId={}", savedMember.getId());

        // Save photo if provided
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                Photo photo = integratedPhotoService.savePhotoForEntity(
                        "board_members",
                        savedMember.getId(),
                        photoFile,
                        uploadedBy,
                        "Board member profile photo - " + member.getFullName()
                );

                if (photo != null) {
                    savedMember.setProfileImageUrl(photo.getPhotoUrl());
                    boardMemberRepository.save(savedMember);
                    logger.info("BOARD_MEMBER_PHOTO_SAVED | memberId={} | photoId={}", savedMember.getId(), photo.getId());
                }
            } catch (IOException e) {
                logger.error("BOARD_MEMBER_PHOTO_SAVE_FAILED | error={}", e.getMessage());
                // Continue without photo - don't fail the member creation
            }
        }

        return savedMember;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public BoardMember saveMember(BoardMember member) {
        try {
            return saveMember(member, null, "system");
        } catch (IOException e) {
            logger.error("BOARD_MEMBER_SAVE_ERROR | error={}", e.getMessage());
            return boardMemberRepository.save(member);
        }
    }

    /**
     * Update board member with optional photo upload
     */
    public BoardMember updateMember(Long id, BoardMember member, MultipartFile photoFile, String uploadedBy) throws IOException {
        logger.info("BOARD_MEMBER_UPDATE_WITH_PHOTO | memberId={} | hasPhoto={}",
                   id, photoFile != null && !photoFile.isEmpty());

        if (boardMemberRepository.existsById(id)) {
            member.setId(id);
            BoardMember updatedMember = boardMemberRepository.save(member);
            logger.debug("BOARD_MEMBER_UPDATED | memberId={}", id);

            // Update photo if provided
            if (photoFile != null && !photoFile.isEmpty()) {
                try {
                    // Delete old photo
                    integratedPhotoService.deletePhotoForEntity(id);

                    // Save new photo
                    Photo photo = integratedPhotoService.savePhotoForEntity(
                            "board_members",
                            id,
                            photoFile,
                            uploadedBy,
                            "Board member profile photo - " + member.getFullName()
                    );

                    if (photo != null) {
                        updatedMember.setProfileImageUrl(photo.getPhotoUrl());
                        boardMemberRepository.save(updatedMember);
                        logger.info("BOARD_MEMBER_PHOTO_UPDATED | memberId={} | photoId={}", id, photo.getId());
                    }
                } catch (IOException e) {
                    logger.error("BOARD_MEMBER_PHOTO_UPDATE_FAILED | error={}", e.getMessage());
                    // Continue without photo update
                }
            }

            return updatedMember;
        }
        return null;
    }

    /**
     * Overloaded method for backwards compatibility (without photo)
     */
    public BoardMember updateMember(Long id, BoardMember member) {
        try {
            return updateMember(id, member, null, "system");
        } catch (IOException e) {
            logger.error("BOARD_MEMBER_UPDATE_ERROR | error={}", e.getMessage());
            if (boardMemberRepository.existsById(id)) {
                member.setId(id);
                return boardMemberRepository.save(member);
            }
            return null;
        }
    }

    /**
     * Delete board member (also deletes associated photos)
     */
    public void deleteMember(Long id) {
        logger.info("BOARD_MEMBER_DELETE | memberId={}", id);
        // Delete photos
        integratedPhotoService.deletePhotoForEntity(id);
        // Delete board member
        boardMemberRepository.deleteById(id);
    }

    // Additional methods for admin controller
    public List<BoardMember> getAllBoardMembers() {
        return getAllMembers();
    }

    public BoardMember getBoardMemberById(Long id) {
        return getMemberById(id);
    }

    public BoardMember saveBoardMember(BoardMember member) {
        return saveMember(member);
    }

    public BoardMember updateBoardMember(Long id, BoardMember member) {
        return updateMember(id, member);
    }

    public void deleteBoardMember(Long id) {
        deleteMember(id);
    }
}

