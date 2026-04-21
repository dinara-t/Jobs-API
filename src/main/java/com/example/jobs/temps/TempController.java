package com.example.jobs.temps;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobs.common.dto.PageResponse;
import com.example.jobs.temps.dtos.TempCreateDto;
import com.example.jobs.temps.dtos.TempResponseDto;
import com.example.jobs.temps.dtos.TempUpdateDto;
import com.example.jobs.temps.dtos.TempWithJobsResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/temps")
public class TempController {

    private final TempService tempService;

    public TempController(TempService tempService) {
        this.tempService = tempService;
    }

    @PostMapping
    public TempResponseDto create(@Valid @RequestBody TempCreateDto dto) {
        return tempService.create(dto);
    }

    @GetMapping("/me")
    public TempResponseDto getProfile() {
        return tempService.getProfile();
    }

    @PatchMapping("/me")
    public TempResponseDto updateProfile(@Valid @RequestBody TempUpdateDto dto) {
        return tempService.updateProfile(dto);
    }

    @GetMapping
    public PageResponse<TempResponseDto> list(
            @RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (jobId != null) {
            return tempService.listAvailableForJob(jobId, sortBy, sortDir, page, size);
        }

        return tempService.listAll(sortBy, sortDir, page, size);
    }

    @GetMapping("/{id:\\d+}")
    public TempWithJobsResponseDto getById(@PathVariable long id) {
        return tempService.getById(id);
    }

    @PatchMapping("/{id:\\d+}")
    public TempResponseDto update(
            @PathVariable long id,
            @Valid @RequestBody TempUpdateDto dto
    ) {
        return tempService.update(id, dto);
    }
}