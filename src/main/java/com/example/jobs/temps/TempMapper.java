package com.example.jobs.temps;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.jobs.temps.dtos.JobSummaryDto;
import com.example.jobs.temps.dtos.TempResponseDto;
import com.example.jobs.temps.dtos.TempWithJobsResponseDto;
import com.example.jobs.temps.entities.Temp;

@Component
public class TempMapper {

    public TempResponseDto toDto(Temp temp) {
        Long managerId = temp.getManager() != null ? temp.getManager().getId() : null;
        long jobCount = temp.getJobs() == null ? 0 : temp.getJobs().size();

        return new TempResponseDto(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                temp.getEmail(),
                managerId,
                jobCount
        );
    }

    public TempWithJobsResponseDto toWithJobsDto(Temp temp) {
        Long managerId = temp.getManager() != null ? temp.getManager().getId() : null;

        List<JobSummaryDto> jobs = temp.getJobs()
                .stream()
                .map(job -> new JobSummaryDto(
                        job.getId(),
                        job.getName(),
                        job.getStartDate(),
                        job.getEndDate()
                ))
                .toList();

        return new TempWithJobsResponseDto(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                temp.getEmail(),
                managerId,
                jobs
        );
    }
}