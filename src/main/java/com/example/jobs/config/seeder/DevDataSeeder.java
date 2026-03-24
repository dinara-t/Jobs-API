package com.example.jobs.config.seeder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.jobs.config.factory.temp.TempFactory;
import com.example.jobs.config.factory.temp.TempFactoryOptions;
import com.example.jobs.jobs.JobRepository;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;
import com.github.javafaker.Faker;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final int MANAGER_COUNT = 20;
    private static final int EMPLOYEE_COUNT = 80;
    private static final int JOB_COUNT = 100;

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final TempFactory tempFactory;
    private final Faker faker = new Faker();

    public DevDataSeeder(
            TempRepository tempRepository,
            JobRepository jobRepository,
            TempFactory tempFactory
    ) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.tempFactory = tempFactory;
    }

    @Override
    public void run(String... args) {
        Temp admin = tempRepository.findByEmail("admin@example.com")
                .orElseGet(() -> tempFactory.createAndPersist(
                        new TempFactoryOptions()
                                .firstName("Admin")
                                .lastName("User")
                                .email("admin@example.com")
                                .rawPassword("admin12345")
                ));

        List<Temp> managers = new ArrayList<>();
        List<Temp> existingTemps = tempRepository.findAll();

        for (Temp temp : existingTemps) {
            Temp manager = temp.getManager();
            if (manager != null && manager.getId() != null && manager.getId().equals(admin.getId())) {
                managers.add(temp);
            }
        }

        while (managers.size() < MANAGER_COUNT) {
            Temp manager = tempFactory.createAndPersist(
                    new TempFactoryOptions().manager(admin)
            );
            managers.add(manager);
        }

        List<Temp> assignableTemps = new ArrayList<>();
        assignableTemps.addAll(managers);

        int employeeCount = 0;
        existingTemps = tempRepository.findAll();

        for (Temp temp : existingTemps) {
            Temp manager = temp.getManager();
            if (manager != null && manager.getId() != null && !manager.getId().equals(admin.getId())) {
                employeeCount++;
                assignableTemps.add(temp);
            }
        }

        while (employeeCount < EMPLOYEE_COUNT) {
            Temp randomManager = managers.get(ThreadLocalRandom.current().nextInt(managers.size()));

            Temp temp = tempFactory.createAndPersist(
                    new TempFactoryOptions().manager(randomManager)
            );

            assignableTemps.add(temp);
            employeeCount++;
        }

        long existingJobs = jobRepository.count();

        while (existingJobs < JOB_COUNT) {
            Job job = new Job();
            job.setName(randomJobName());

            LocalDate start = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 31));
            LocalDate end = start.plusDays(ThreadLocalRandom.current().nextInt(0, 6));

            job.setStartDate(start);
            job.setEndDate(end);

            boolean assignJob = ThreadLocalRandom.current().nextInt(100) < 75;
            if (assignJob && !assignableTemps.isEmpty()) {
                Temp randomTemp = assignableTemps.get(ThreadLocalRandom.current().nextInt(assignableTemps.size()));
                job.setTemp(randomTemp);
            }

            jobRepository.save(job);
            existingJobs++;
        }
    }

    private String randomJobName() {
        return faker.job().title();
    }
}