package com.example.jobs.temps;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jobs.auth.CurrentTempService;
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
    public List<TempResponseDto> listAll() {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> visibleIds = tempHierarchyService.getDescendantIds(current);

        return tempRepository.findAll()
                .stream()
                .filter(t -> visibleIds.contains(t.getId()))
                .sorted(Comparator.comparing(Temp::getFirstName).thenComparing(Temp::getLastName).thenComparing(Temp::getId))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TempWithJobsResponseDto getById(long id) {
        Temp current = currentTempService.getCurrentTempEntity();
        Temp temp = tempRepository.findById(id).orElseThrow(() -> new NotFoundException("Temp not found"));

        if (!tempHierarchyService.isStrictDescendant(temp.getId(), current)) {
            throw new NotFoundException("Temp not found");
        }

        return toWithJobsDto(temp);
    }

    @Transactional
    public TempResponseDto update(long id, TempUpdateDto dto) {
        Temp current = currentTempService.getCurrentTempEntity();
        Temp target = tempRepository.findById(id).orElseThrow(() -> new NotFoundException("Temp not found"));

        if (!tempHierarchyService.isStrictDescendant(target.getId(), current)) {
            throw new NotFoundException("Temp not found");
        }

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
    public List<TempResponseDto> listAvailableForJob(long jobId) {
        Temp current = currentTempService.getCurrentTempEntity();
        Set<Long> assignableIds = tempHierarchyService.getSelfAndDescendantIds(current);

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        ensureJobVisible(job, assignableIds);

        return tempRepository.findAll()
                .stream()
                .filter(t -> assignableIds.contains(t.getId()))
                .filter(t -> isAvailableForRange(t.getId(), job.getStartDate(), job.getEndDate(), job.getId()))
                .sorted(Comparator.comparing(Temp::getFirstName).thenComparing(Temp::getLastName).thenComparing(Temp::getId))
                .map(this::toDto)
                .toList();
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

    private boolean isAvailableForRange(long tempId, LocalDate start, LocalDate end, long excludeJobId) {
        long overlaps = jobRepository.countOverlappingJobsForTempExcludingJob(tempId, excludeJobId, start, end);
        return overlaps == 0;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private TempResponseDto toDto(Temp temp) {
        Long managerId = temp.getManager() != null ? temp.getManager().getId() : null;
        return new TempResponseDto(
                temp.getId(),
                temp.getFirstName(),
                temp.getLastName(),
                temp.getEmail(),
                managerId
        );
    }

    private TempWithJobsResponseDto toWithJobsDto(Temp temp) {
        List<JobSummaryDto> jobs = temp.getJobs()
                .stream()
                .sorted(Comparator.comparing(Job::getStartDate).thenComparing(Job::getId))
                .map(j -> new JobSummaryDto(j.getId(), j.getName(), j.getStartDate(), j.getEndDate()))
                .toList();

        Long managerId = temp.getManager() != null ? temp.getManager().getId() : null;

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