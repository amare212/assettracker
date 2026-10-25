package com.def.assettracker.service;

import com.def.assettracker.entity.*;
import com.def.assettracker.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;
    private final AppUserRepository appUserRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssetRepository assetRepository,
            AppUserRepository appUserRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assetRepository = assetRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<Assignment> getAll() {
        return assignmentRepository.findAll();
    }

    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    public List<Assignment> getByEmployee(Long employeeId) {
        return assignmentRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public Assignment assignAsset(Long assetId, Long employeeId, String notes) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Asset not found"));

        AppUser employee = appUserRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found"));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Asset is not available for assignment");
        }

        Assignment assignment = new Assignment();
        assignment.setAsset(asset);
        assignment.setEmployee(employee);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setNotes(notes);

        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment returnAsset(Long assignmentId) {
        Assignment assignment = getById(assignmentId);

        if (assignment.getStatus() == AssignmentStatus.RETURNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Asset has already been returned");
        }

        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnedDate(LocalDate.now());

        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);

        assetRepository.save(asset);
        return assignmentRepository.save(assignment);
    }
}