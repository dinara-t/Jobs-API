package com.example.jobs.temps.dtos;

import java.util.List;

public class TempWithJobsResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long managerId;
    private List<JobSummaryDto> jobs;

    public TempWithJobsResponseDto() {
    }

    public TempWithJobsResponseDto(Long id, String firstName, String lastName, String email, Long managerId, List<JobSummaryDto> jobs) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.managerId = managerId;
        this.jobs = jobs;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Long getManagerId() {
        return managerId;
    }

    public List<JobSummaryDto> getJobs() {
        return jobs;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public void setJobs(List<JobSummaryDto> jobs) {
        this.jobs = jobs;
    }
}