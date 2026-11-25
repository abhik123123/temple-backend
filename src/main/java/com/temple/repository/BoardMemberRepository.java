package com.temple.repository;

import com.temple.entity.BoardMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    List<BoardMember> findAllByOrderByPositionAscFullNameAsc();

    List<BoardMember> findByDepartmentIgnoreCaseOrderByPositionAscFullNameAsc(String department);
}
