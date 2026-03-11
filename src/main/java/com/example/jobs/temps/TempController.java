package com.example.jobs.temps;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobs.temps.dtos.TempCreateDto;
import com.example.jobs.temps.dtos.TempResponseDto;
import com.example.jobs.temps.dtos.TempUpdateDto;
import com.example.jobs.temps.dtos.TempWithJobsResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class TempController {

    private final TempService tempService;

    public TempController(TempService tempService) {
        this.tempService = tempService;
    }

    @PostMapping("/temps")
    public TempResponseDto create(@Valid @RequestBody TempCreateDto dto) {
        return tempService.create(dto);
    }

    @GetMapping("/temps")
    public List<TempResponseDto> list(@RequestParam(required = false) Long jobId) {
        if (jobId != null) {
            return tempService.listAvailableForJob(jobId);
        }
        return tempService.listAll();
    }

    @GetMapping("/temps/{id}")
    public TempWithJobsResponseDto get(@PathVariable long id) {
        return tempService.getById(id);
    }

    @PatchMapping("/temps/{id}")
    public TempResponseDto update(@PathVariable long id, @Valid @RequestBody TempUpdateDto dto) {
        return tempService.update(id, dto);
    }

    @GetMapping("/profile")
    public TempResponseDto getProfile() {
        return tempService.getProfile();
    }

    @PatchMapping("/profile")
    public TempResponseDto updateProfile(@Valid @RequestBody TempUpdateDto dto) {
        return tempService.updateProfile(dto);
    }
}