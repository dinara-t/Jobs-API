package com.example.jobs.jobs.dtos;

import java.time.LocalDate;

public class JobUpdateDto {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long tempId;

    public JobUpdateDto() {
    }

    public JobUpdateDto(String name, LocalDate startDate, LocalDate endDate, Long tempId) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tempId = tempId;
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

    public Long getTempId() {
        return tempId;
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

    public void setTempId(Long tempId) {
        this.tempId = tempId;
    }
}