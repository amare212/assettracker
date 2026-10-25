package com.def.assettracker.repository;

import com.def.assettracker.entity.Assignment;
import com.def.assettracker.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    List<Assignment> findByEmployeeId(Long employeeId);

    List<Assignment> findByAssetId(Long assetId);

    List<Assignment> findByStatus(AssignmentStatus status);
}