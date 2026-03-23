package com.example.jobs.temps;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.jobs.temps.entities.Temp;

public interface TempRepository extends JpaRepository<Temp, Long> {

    Optional<Temp> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Temp> findAllByIdInOrderByFirstNameAscLastNameAscIdAsc(Collection<Long> ids);

    Optional<Temp> findByIdAndIdIn(Long id, Collection<Long> ids);

    @Query("""
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
        order by t.firstName asc, t.lastName asc, t.id asc
    """)
    List<Temp> findAvailableTempsForRange(
            Collection<Long> tempIds,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeJobId
    );
}