package com.example.jobs.temps;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.jobs.temps.entities.Temp;

public interface TempRepository extends JpaRepository<Temp, Long> {

    Optional<Temp> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Temp> findByIdAndIdIn(Long id, Collection<Long> ids);

    @Query(
        value = """
            select t
            from Temp t
            where t.id in :ids
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :ids
            """
    )
    Page<Temp> findVisibleTemps(Collection<Long> ids, Pageable pageable);

    @Query(
        value = """
            select t
            from Temp t
            left join t.jobs j
            where t.id in :ids
            group by t
            order by count(j) asc, t.firstName asc, t.lastName asc, t.id asc
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :ids
            """
    )
    Page<Temp> findVisibleTempsOrderByJobCountAsc(Collection<Long> ids, Pageable pageable);

    @Query(
        value = """
            select t
            from Temp t
            left join t.jobs j
            where t.id in :ids
            group by t
            order by count(j) desc, t.firstName asc, t.lastName asc, t.id asc
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :ids
            """
    )
    Page<Temp> findVisibleTempsOrderByJobCountDesc(Collection<Long> ids, Pageable pageable);

    @Query(
        value = """
            select t
            from Temp t
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job j
                  where j.temp = t
                    and j.id <> :excludeJobId
                    and j.startDate <= :endDate
                    and j.endDate >= :startDate
              )
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job j
                  where j.temp = t
                    and j.id <> :excludeJobId
                    and j.startDate <= :endDate
                    and j.endDate >= :startDate
              )
            """
    )
    Page<Temp> findAvailableTempsForRange(
            Collection<Long> tempIds,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeJobId,
            Pageable pageable
    );

    @Query(
        value = """
            select t
            from Temp t
            left join t.jobs j
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job blocked
                  where blocked.temp = t
                    and blocked.id <> :excludeJobId
                    and blocked.startDate <= :endDate
                    and blocked.endDate >= :startDate
              )
            group by t
            order by count(j) asc, t.firstName asc, t.lastName asc, t.id asc
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job blocked
                  where blocked.temp = t
                    and blocked.id <> :excludeJobId
                    and blocked.startDate <= :endDate
                    and blocked.endDate >= :startDate
              )
            """
    )
    Page<Temp> findAvailableTempsForRangeOrderByJobCountAsc(
            Collection<Long> tempIds,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeJobId,
            Pageable pageable
    );

    @Query(
        value = """
            select t
            from Temp t
            left join t.jobs j
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job blocked
                  where blocked.temp = t
                    and blocked.id <> :excludeJobId
                    and blocked.startDate <= :endDate
                    and blocked.endDate >= :startDate
              )
            group by t
            order by count(j) desc, t.firstName asc, t.lastName asc, t.id asc
            """,
        countQuery = """
            select count(t)
            from Temp t
            where t.id in :tempIds
              and not exists (
                  select 1
                  from Job blocked
                  where blocked.temp = t
                    and blocked.id <> :excludeJobId
                    and blocked.startDate <= :endDate
                    and blocked.endDate >= :startDate
              )
            """
    )
    Page<Temp> findAvailableTempsForRangeOrderByJobCountDesc(
            Collection<Long> tempIds,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeJobId,
            Pageable pageable
    );
}