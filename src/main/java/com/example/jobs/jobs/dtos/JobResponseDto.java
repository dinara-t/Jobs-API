package com.example.jobs.jobs.dtos;

import java.time.LocalDate;

public class JobResponseDto {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private JobTempDto temp;

    public JobResponseDto() {
    }

    public JobResponseDto(Long id, String name, LocalDate startDate, LocalDate endDate, JobTempDto temp) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.temp = temp;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public JobTempDto getTemp() {
        return temp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTemp(JobTempDto temp) {
        this.temp = temp;
    }
}