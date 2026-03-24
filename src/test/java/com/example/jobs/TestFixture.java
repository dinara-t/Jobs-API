package com.example.jobs;

import com.example.jobs.jobs.JobRepository;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("test")
public class TestFixture {

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    private Temp admin;
    private Temp managerA;
    private Temp workerA1;
    private Temp workerA2;
    private Temp managerB;
    private Temp workerB1;

    private Job unassignedVisibleJob;
    private Job visibleAssignedJob;
    private Job invisibleAssignedJob;
    private Job workerA1ExistingJob;

    private final String defaultPassword = "password12345";
    private final String adminPassword = "admin12345";

    public TestFixture(
            TempRepository tempRepository,
            JobRepository jobRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void seed() {
        jobRepository.deleteAll();
        tempRepository.deleteAll();

        admin = createTemp("Admin", "User", "admin@example.com", adminPassword, null);

        managerA = createTemp("Manager", "Alpha", "manager.alpha@example.com", defaultPassword, admin);
        workerA1 = createTemp("Worker", "AlphaOne", "worker.alpha1@example.com", defaultPassword, managerA);
        workerA2 = createTemp("Worker", "AlphaTwo", "worker.alpha2@example.com", defaultPassword, managerA);

        managerB = createTemp("Manager", "Beta", "manager.beta@example.com", defaultPassword, admin);
        workerB1 = createTemp("Worker", "BetaOne", "worker.beta1@example.com", defaultPassword, managerB);

        unassignedVisibleJob = createJob(
                "Unassigned Visible Job",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                null
        );

        visibleAssignedJob = createJob(
                "Visible Assigned Job",
                LocalDate.of(2026, 4, 15),
                LocalDate.of(2026, 4, 16),
                workerA2
        );

        invisibleAssignedJob = createJob(
                "Invisible Assigned Job",
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 21),
                workerB1
        );

        workerA1ExistingJob = createJob(
                "Worker A1 Existing Job",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                workerA1
        );
    }

    private Temp createTemp(String firstName, String lastName, String email, String rawPassword, Temp manager) {
        Temp temp = new Temp();
        temp.setFirstName(firstName);
        temp.setLastName(lastName);
        temp.setEmail(email);
        temp.setPasswordHash(passwordEncoder.encode(rawPassword));
        temp.setManager(manager);
        return tempRepository.save(temp);
    }

    private Job createJob(String name, LocalDate startDate, LocalDate endDate, Temp temp) {
        Job job = new Job();
        job.setName(name);
        job.setStartDate(startDate);
        job.setEndDate(endDate);
        job.setTemp(temp);
        return jobRepository.save(job);
    }

    public Temp getAdmin() {
        return admin;
    }

    public Temp getManagerA() {
        return managerA;
    }

    public Temp getWorkerA1() {
        return workerA1;
    }

    public Temp getWorkerA2() {
        return workerA2;
    }

    public Temp getManagerB() {
        return managerB;
    }

    public Temp getWorkerB1() {
        return workerB1;
    }

    public Job getUnassignedVisibleJob() {
        return unassignedVisibleJob;
    }

    public Job getVisibleAssignedJob() {
        return visibleAssignedJob;
    }

    public Job getInvisibleAssignedJob() {
        return invisibleAssignedJob;
    }

    public Job getWorkerA1ExistingJob() {
        return workerA1ExistingJob;
    }

    public String getPasswordFor(Temp temp) {
        if (temp == null || temp.getEmail() == null) {
            throw new IllegalArgumentException("Temp must not be null");
        }

        if ("admin@example.com".equals(temp.getEmail())) {
            return adminPassword;
        }

        return defaultPassword;
    }
}