package com.example.jobs.jobs;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobs.auth.CurrentTempService;
import com.example.jobs.common.exception.BadRequestException;
import com.example.jobs.common.exception.NotFoundException;
import com.example.jobs.jobs.dtos.JobCreateDto;
import com.example.jobs.jobs.dtos.JobResponseDto;
import com.example.jobs.jobs.dtos.JobTempDto;
import com.example.jobs.jobs.dtos.JobUpdateDto;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.TempHierarchyService;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final TempRepository tempRepository;
    private final CurrentTempService currentTempService;
    private final TempHierarchyService tempHierarchyService;

    public JobService(
            JobRepository jobRepository,
            TempRepository tempRepository,
            CurrentTempService currentTempService,
            TempHierarchyService tempHierarchyService
    ) {
        this.jobRepository = jobRepository;
        this.tempRepository = tempRepository;
        this.currentTempService = currentTempService;
        this.tempHierarchyService = tempHierarchyService;
    }

    @Transactional
    public JobResponseDto create(JobCreateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleTempIds = tempHierarchyService.getSelfAndDescendantIds(current);

        validateDateRange(dto.getStartDate(), dto.getEndDate());

        Job job = new Job();
        job.setName(dto.getName());
        job.setStartDate(dto.getStartDate());
        job.setEndDate(dto.getEndDate());

        if (dto.getTempId() != null) {
            Temp temp = tempRepository.findById(dto.getTempId())
                    .orElseThrow(() -> new NotFoundException("Temp not found"));

            if (!visibleTempIds.contains(temp.getId())) {
                throw new NotFoundException("Temp not found");
            }

            ensureTempAvailableForRange(temp.getId(), dto.getStartDate(), dto.getEndDate(), 0L);
            job.setTemp(temp);
        }

        Job saved = jobRepository.save(job);
        return toDto(saved);
    }

    @Transactional
    public JobResponseDto update(long id, JobUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleTempIds = tempHierarchyService.getSelfAndDescendantIds(current);

        Job job = jobRepository.findVisibleById(id, visibleTempIds)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        LocalDate newStart = dto.getStartDate() != null ? dto.getStartDate() : job.getStartDate();
        LocalDate newEnd = dto.getEndDate() != null ? dto.getEndDate() : job.getEndDate();
        validateDateRange(newStart, newEnd);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            job.setName(dto.getName());
        }

        job.setStartDate(newStart);
        job.setEndDate(newEnd);

        if (dto.getTempId() != null) {
            if (dto.getTempId() <= 0) {
                job.setTemp(null);
            } else {
                Temp temp = tempRepository.findById(dto.getTempId())
                        .orElseThrow(() -> new NotFoundException("Temp not found"));

                if (!visibleTempIds.contains(temp.getId())) {
                    throw new NotFoundException("Temp not found");
                }

                ensureTempAvailableForRange(temp.getId(), newStart, newEnd, job.getId());
                job.setTemp(temp);
            }
        }

        return toDto(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<JobResponseDto> list(Boolean assigned) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleTempIds = tempHierarchyService.getSelfAndDescendantIds(current);

        return jobRepository.findVisibleJobs(visibleTempIds, assigned)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobResponseDto get(long id) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleTempIds = tempHierarchyService.getSelfAndDescendantIds(current);

        Job job = jobRepository.findVisibleById(id, visibleTempIds)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        return toDto(job);
    }

    private void ensureTempAvailableForRange(long tempId, LocalDate startDate, LocalDate endDate, long excludeJobId) {
        long overlaps = excludeJobId > 0
                ? jobRepository.countOverlappingJobsForTempExcludingJob(tempId, excludeJobId, startDate, endDate)
                : jobRepository.countOverlappingJobsForTemp(tempId, startDate, endDate);

        if (overlaps > 0) {
            throw new BadRequestException("Temp already has an overlapping job");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be on or after start date");
        }
    }

    private JobResponseDto toDto(Job job) {
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