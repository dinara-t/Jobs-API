package com.example.jobs.temps;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobs.auth.CurrentTempService;
import com.example.jobs.common.dto.PageResponse;
import com.example.jobs.common.exception.BadRequestException;
import com.example.jobs.common.exception.NotFoundException;
import com.example.jobs.jobs.JobRepository;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.dtos.JobSummaryDto;
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

    public TempService(
            TempRepository tempRepository,
            JobRepository jobRepository,
            CurrentTempService currentTempService,
            TempHierarchyService tempHierarchyService,
            PasswordEncoder passwordEncoder
    ) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.currentTempService = currentTempService;
        this.tempHierarchyService = tempHierarchyService;
        this.passwordEncoder = passwordEncoder;
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
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TempResponseDto> listAll(String sortBy, String sortDir, int page, int size) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        Page<TempResponseDto> result = findTempsPage(visibleIds, sortBy, sortDir, page, size)
                .map(this::toDto);

        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public TempWithJobsResponseDto getById(long id) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        Temp temp = tempRepository.findByIdAndIdIn(id, visibleIds)
                .orElseThrow(() -> new NotFoundException("Temp not found"));

        return toWithJobsDto(temp);
    }

    @Transactional
    public TempResponseDto update(long id, TempUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        Temp target = tempRepository.findByIdAndIdIn(id, visibleIds)
                .orElseThrow(() -> new NotFoundException("Temp not found"));

        applyUpdate(target, dto, current);
        Temp saved = tempRepository.save(target);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public TempResponseDto getProfile() {
        Temp current = currentTempService.getCurrentTempEntity();
        return toDto(current);
    }

    @Transactional
    public TempResponseDto updateProfile(TempUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        applyUpdate(current, dto, current);
        Temp saved = tempRepository.save(current);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<TempResponseDto> listAvailableForJob(long jobId, String sortBy, String sortDir, int page, int size) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> assignableIds = tempHierarchyService.getSelfAndDescendantIds(current);

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        ensureJobVisible(job, assignableIds);

        Page<TempResponseDto> result = findAvailableTempsPage(
                        assignableIds,
                        job.getStartDate(),
                        job.getEndDate(),
                        job.getId(),
                        sortBy,
                        sortDir,
                        page,
                        size
                )
                .map(this::toDto);

        return PageResponse.from(result);
    }

    private Page<Temp> findTempsPage(Set<Long> visibleIds, String sortBy, String sortDir, int page, int size) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = parseDirection(sortDir);
        PageRequest pageable = PageRequest.of(normalizePage(page), normalizeSize(size), buildSort(normalizedSortBy, direction));

        if ("jobcount".equals(normalizedSortBy)) {
            if (direction == Sort.Direction.DESC) {
                return tempRepository.findVisibleTempsOrderByJobCountDesc(visibleIds, pageable);
            }
            return tempRepository.findVisibleTempsOrderByJobCountAsc(visibleIds, pageable);
        }

        return tempRepository.findVisibleTemps(visibleIds, pageable);
    }

    private Page<Temp> findAvailableTempsPage(
            Set<Long> assignableIds,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            Long excludeJobId,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = parseDirection(sortDir);
        PageRequest pageable = PageRequest.of(normalizePage(page), normalizeSize(size), buildSort(normalizedSortBy, direction));

        if ("jobcount".equals(normalizedSortBy)) {
            if (direction == Sort.Direction.DESC) {
                return tempRepository.findAvailableTempsForRangeOrderByJobCountDesc(
                        assignableIds,
                        startDate,
                        endDate,
                        excludeJobId,
                        pageable
                );
            }
            return tempRepository.findAvailableTempsForRangeOrderByJobCountAsc(
                    assignableIds,
                    startDate,
                    endDate,
                    excludeJobId,
                    pageable
            );
        }

        return tempRepository.findAvailableTempsForRange(
                assignableIds,
                startDate,
                endDate,
                excludeJobId,
                pageable
        );
    }

    private Sort buildSort(String sortBy, Sort.Direction direction) {
        if ("id".equals(sortBy)) {
            return Sort.by(
                    new Sort.Order(direction, "id")
            );
        }

        return Sort.by(
                new Sort.Order(direction, "firstName"),
                new Sort.Order(direction, "lastName"),
                new Sort.Order(direction, "id")
        );
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "name";
        }

        String normalized = sortBy.trim().toLowerCase();
        if ("id".equals(normalized) || "name".equals(normalized) || "jobcount".equals(normalized)) {
            return normalized;
        }

        throw new BadRequestException("Invalid temps sortBy value");
    }

    private Sort.Direction parseDirection(String sortDir) {
        try {
            return Sort.Direction.fromString(sortDir == null || sortDir.isBlank() ? "asc" : sortDir.trim());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid sort direction");
        }
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

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private TempResponseDto toDto(Temp temp) {
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

    private TempWithJobsResponseDto toWithJobsDto(Temp temp) {
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