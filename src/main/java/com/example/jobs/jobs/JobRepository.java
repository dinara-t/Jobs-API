package com.example.jobs.jobs;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.jobs.jobs.entities.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByTempIsNull();

    List<Job> findAllByTempIsNotNull();

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