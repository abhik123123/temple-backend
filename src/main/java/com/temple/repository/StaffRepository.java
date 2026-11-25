package com.temple.repository;

import com.temple.entity.Staff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findAllByOrderByPositionAscFullNameAsc();

    List<Staff> findByDepartmentIgnoreCaseOrderByPositionAscFullNameAsc(String department);
}
