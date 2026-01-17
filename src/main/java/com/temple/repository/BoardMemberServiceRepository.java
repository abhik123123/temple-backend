package com.temple.repository;

import com.temple.entity.BoardMemberService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardMemberServiceRepository extends JpaRepository<BoardMemberService, Long> {
    List<BoardMemberService> findByBoardMemberId(Long boardMemberId);
    List<BoardMemberService> findByStatus(String status);
}

