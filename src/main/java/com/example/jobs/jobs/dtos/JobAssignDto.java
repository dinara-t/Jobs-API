package com.example.jobs.jobs.dtos;

import jakarta.validation.constraints.NotNull;

public class JobAssignDto {

    @NotNull
    private Long assignedTempId;

    public JobAssignDto() {
    }

    public JobAssignDto(Long assignedTempId) {
        this.assignedTempId = assignedTempId;
    }

    public Long getAssignedTempId() {
        return assignedTempId;
    }

    public void setAssignedTempId(Long assignedTempId) {
        this.assignedTempId = assignedTempId;
    }
}