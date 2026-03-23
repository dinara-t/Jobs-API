package com.example.jobs.jobs;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.jobs.jobs.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
        select j
        from Job j
        left join fetch j.temp t
        where (t is null or t.id in :visibleTempIds)
          and (
                :assigned is null
                or (:assigned = true and t is not null)
                or (:assigned = false and t is null)
              )
        order by j.startDate asc, j.id asc
    """)
    List<Job> findVisibleJobs(Collection<Long> visibleTempIds, Boolean assigned);

    @Query("""
        select j
        from Job j
        left join fetch j.temp t
        where j.id = :id
          and (t is null or t.id in :visibleTempIds)
    """)
    Optional<Job> findVisibleById(Long id, Collection<Long> visibleTempIds);

    @Query("""
        select count(j) from Job j
        where j.temp.id = :tempId
          and j.id <> :excludeJobId
          and j.startDate <= :endDate
          and j.endDate >= :startDate
    """)
    long countOverlappingJobsForTempExcludingJob(long tempId, long excludeJobId, LocalDate startDate, LocalDate endDate);

    @Query("""
        select count(j) from Job j
        where j.temp.id = :tempId
          and j.startDate <= :endDate
          and j.endDate >= :startDate
    """)
    long countOverlappingJobsForTemp(long tempId, LocalDate startDate, LocalDate endDate);
}