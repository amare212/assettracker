package com.def.assettracker.controller;

import com.def.assettracker.entity.Assignment;
import com.def.assettracker.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<Assignment> getAll() {
        return assignmentService.getAll();
    }

    @GetMapping("/{id}")
    public Assignment getById(@PathVariable Long id) {
        return assignmentService.getById(id);
    }

    @GetMapping("/employee/{employeeId}")
    public List<Assignment> getByEmployee(
            @PathVariable Long employeeId) {
        return assignmentService.getByEmployee(employeeId);
    }

    public record AssignmentRequest(
            Long assetId,
            Long employeeId,
            String notes) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assignment assignAsset(
            @RequestBody AssignmentRequest request) {
        if (request.assetId() == null || request.employeeId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Asset ID and employee ID are required");
        }

        return assignmentService.assignAsset(
                request.assetId(),
                request.employeeId(),
                request.notes());
    }

    @PostMapping("/{id}/return")
    public Assignment returnAsset(@PathVariable Long id) {
        return assignmentService.returnAsset(id);
    }
}