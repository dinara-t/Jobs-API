package com.example.jobs.jobs;

import org.springframework.stereotype.Component;

import com.example.jobs.jobs.dtos.JobResponseDto;
import com.example.jobs.jobs.dtos.JobTempDto;
import com.example.jobs.jobs.entities.Job;

@Component
public class JobMapper {

    public JobResponseDto toDto(Job job) {
        JobTempDto tempDto = null;

        if (job.getTemp() != null) {
            tempDto = new JobTempDto(
                    job.getTemp().getId(),
                    job.getTemp().getFirstName(),
                    job.getTemp().getLastName()
            );
        }

        return new JobResponseDto(
                job.getId(),
                job.getName(),
                job.getStartDate(),
                job.getEndDate(),
                tempDto
        );
    }
}