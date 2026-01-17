package com.temple.repository;

import com.temple.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    @Query("SELECT s FROM Staff s WHERE s.department = :department ORDER BY s.position")
    List<Staff> findByDepartment(String department);

    @Query("SELECT s FROM Staff s ORDER BY s.position")
    List<Staff> findAllOrderByPosition();
}

