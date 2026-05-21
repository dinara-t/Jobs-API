package com.example.jobs.temps;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.jobs.common.exception.BadRequestException;
import com.example.jobs.temps.entities.Temp;

@Service
public class TempQueryService {

    private final TempRepository tempRepository;

    public TempQueryService(TempRepository tempRepository) {
        this.tempRepository = tempRepository;
    }

    public Page<Temp> findTempsPage(
            Set<Long> visibleIds,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = parseDirection(sortDir);
        PageRequest pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                buildSort(normalizedSortBy, direction)
        );

        if ("jobcount".equals(normalizedSortBy)) {
            if (direction == Sort.Direction.DESC) {
                return tempRepository.findVisibleTempsOrderByJobCountDesc(visibleIds, pageable);
            }

            return tempRepository.findVisibleTempsOrderByJobCountAsc(visibleIds, pageable);
        }

        return tempRepository.findVisibleTemps(visibleIds, pageable);
    }

    public Page<Temp> findAvailableTempsPage(
            Set<Long> assignableIds,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeJobId,
            String sortBy,
            String sortDir,
            int page,
            int size
    ) {
        String normalizedSortBy = normalizeSortBy(sortBy);
        Sort.Direction direction = parseDirection(sortDir);
        PageRequest pageable = PageRequest.of(
                normalizePage(page),
                normalizeSize(size),
                buildSort(normalizedSortBy, direction)
        );

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
            return Sort.by(new Sort.Order(direction, "id"));
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
            return Sort.Direction.fromString(
                    sortDir == null || sortDir.isBlank() ? "asc" : sortDir.trim()
            );
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid sort direction");
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
}