package com.example.jobs.temps;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobs.auth.CurrentTempService;
import com.example.jobs.common.dto.PageResponse;
import com.example.jobs.common.exception.BadRequestException;
import com.example.jobs.common.exception.NotFoundException;
import com.example.jobs.jobs.JobRepository;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.dtos.TempCreateDto;
import com.example.jobs.temps.dtos.TempResponseDto;
import com.example.jobs.temps.dtos.TempUpdateDto;
import com.example.jobs.temps.dtos.TempWithJobsResponseDto;
import com.example.jobs.temps.entities.Temp;

@Service
public class TempService {

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final CurrentTempService currentTempService;
    private final TempHierarchyService tempHierarchyService;
    private final PasswordEncoder passwordEncoder;
    private final TempMapper tempMapper;
    private final TempQueryService tempQueryService;

    public TempService(
            TempRepository tempRepository,
            JobRepository jobRepository,
            CurrentTempService currentTempService,
            TempHierarchyService tempHierarchyService,
            PasswordEncoder passwordEncoder,
            TempMapper tempMapper,
            TempQueryService tempQueryService
    ) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.currentTempService = currentTempService;
        this.tempHierarchyService = tempHierarchyService;
        this.passwordEncoder = passwordEncoder;
        this.tempMapper = tempMapper;
        this.tempQueryService = tempQueryService;
    }

    @Transactional
    public TempResponseDto create(TempCreateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();

        String email = normalizeEmail(dto.getEmail());
        ensureUniqueEmail(email, null);

        Temp manager = resolveManagerForCreate(dto.getManagerId(), current);

        Temp temp = new Temp();
        temp.setFirstName(dto.getFirstName().trim());
        temp.setLastName(dto.getLastName().trim());
        temp.setEmail(email);
        temp.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        temp.setManager(manager);

        Temp saved = tempRepository.save(temp);
        return tempMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TempResponseDto> listAll(String sortBy, String sortDir, int page, int size) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        var result = tempQueryService.findTempsPage(visibleIds, sortBy, sortDir, page, size)
                .map(tempMapper::toDto);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public TempWithJobsResponseDto getById(long id) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        Temp temp = tempRepository.findByIdAndIdIn(id, visibleIds)
                .orElseThrow(() -> new NotFoundException("Temp not found"));

        return tempMapper.toWithJobsDto(temp);
    }

    @Transactional
    public TempResponseDto update(long id, TempUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        Temp target = tempRepository.findByIdAndIdIn(id, visibleIds)
                .orElseThrow(() -> new NotFoundException("Temp not found"));

        applyUpdate(target, dto, current);
        Temp saved = tempRepository.save(target);
        return tempMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public TempResponseDto getProfile() {
        Temp current = currentTempService.getCurrentTempEntity();
        return tempMapper.toDto(current);
    }

    @Transactional
    public TempResponseDto updateProfile(TempUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        applyUpdate(current, dto, current);
        Temp saved = tempRepository.save(current);
        return tempMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TempResponseDto> listAvailableForJob(
            long jobId,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getSelfAndDescendantIds(current);
        Set<Long> assignableIds = tempHierarchyService.getDescendantIds(current);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));

        ensureJobVisible(job, visibleIds);

        var result = tempQueryService.findAvailableTempsPage(
                        assignableIds,
                        job.getStartDate(),
                        job.getEndDate(),
                        job.getId(),
                        sortBy,
                        sortDir,
                        page,
                        size
                )
                .map(tempMapper::toDto);

        return PageResponse.from(result);
    }

    private void applyUpdate(Temp target, TempUpdateDto dto, Temp actingUser) {
        String email = normalizeEmail(dto.getEmail());
        ensureUniqueEmail(email, target.getId());

        Temp manager = resolveManagerForUpdate(dto.getManagerId(), actingUser, target);

        target.setFirstName(dto.getFirstName().trim());
        target.setLastName(dto.getLastName().trim());
        target.setEmail(email);
        target.setManager(manager);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            target.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
    }

    private Temp resolveManagerForCreate(Long managerId, Temp actingUser) {
        if (managerId == null) {
            return actingUser;
        }

        Temp manager = tempRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        if (!tempHierarchyService.isSelfOrDescendant(manager.getId(), actingUser)) {
            throw new NotFoundException("Manager not found");
        }

        return manager;
    }

    private Temp resolveManagerForUpdate(Long managerId, Temp actingUser, Temp target) {
        if (managerId == null) {
            return null;
        }

        if (managerId.equals(target.getId())) {
            throw new BadRequestException("A temp cannot manage themselves");
        }

        Temp manager = tempRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("Manager not found"));

        if (!tempHierarchyService.isSelfOrDescendant(manager.getId(), actingUser)) {
            throw new NotFoundException("Manager not found");
        }

        Set<Long> targetDescendants = tempHierarchyService.getDescendantIds(target);
        if (targetDescendants.contains(manager.getId())) {
            throw new BadRequestException("A temp cannot report to one of their own reports");
        }

        return manager;
    }

    private void ensureUniqueEmail(String email, Long currentId) {
        boolean exists = currentId == null
                ? tempRepository.existsByEmailIgnoreCase(email)
                : tempRepository.existsByEmailIgnoreCaseAndIdNot(email, currentId);

        if (exists) {
            throw new BadRequestException("Email is already in use");
        }
    }

    private void ensureJobVisible(Job job, Set<Long> visibleTempIds) {
        if (job.getTemp() == null) {
            return;
        }

        if (!visibleTempIds.contains(job.getTemp().getId())) {
            throw new NotFoundException("Job not found");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
