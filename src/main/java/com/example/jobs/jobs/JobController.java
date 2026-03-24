package com.example.jobs.jobs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobs.common.dto.PageResponse;
import com.example.jobs.jobs.dtos.JobCreateDto;
import com.example.jobs.jobs.dtos.JobResponseDto;
import com.example.jobs.jobs.dtos.JobUpdateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public JobResponseDto create(@Valid @RequestBody JobCreateDto dto) {
        return jobService.create(dto);
    }

    @PatchMapping("/{id}")
    public JobResponseDto update(@PathVariable long id, @RequestBody JobUpdateDto dto) {
        return jobService.update(id, dto);
    }

    @GetMapping
    public PageResponse<JobResponseDto> list(
            @RequestParam(required = false) Boolean assigned,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.list(assigned, page, size);
    }

    @GetMapping("/{id}")
    public JobResponseDto get(@PathVariable long id) {
        return jobService.get(id);
    }
}