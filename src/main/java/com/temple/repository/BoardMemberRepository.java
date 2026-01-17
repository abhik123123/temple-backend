package com.temple.repository;

import com.temple.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    @Query("SELECT b FROM BoardMember b WHERE b.department = :department ORDER BY b.position")
    List<BoardMember> findByDepartment(String department);

    @Query("SELECT b FROM BoardMember b ORDER BY b.position")
    List<BoardMember> findAllOrderByPosition();
}

