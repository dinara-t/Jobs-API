package com.example.jobs.temps.dtos;

public class TempResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long managerId;
    private long jobCount;

    public TempResponseDto() {
    }

    public TempResponseDto(Long id, String firstName, String lastName, String email, Long managerId, long jobCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.managerId = managerId;
        this.jobCount = jobCount;
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

    public long getJobCount() {
        return jobCount;
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

    public void setJobCount(long jobCount) {
        this.jobCount = jobCount;
    }
}