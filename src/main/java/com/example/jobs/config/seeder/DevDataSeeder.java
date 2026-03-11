package com.example.jobs.config.seeder;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.jobs.jobs.JobRepository;
import com.example.jobs.jobs.entities.Job;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final TempRepository tempRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(TempRepository tempRepository, JobRepository jobRepository, PasswordEncoder passwordEncoder) {
        this.tempRepository = tempRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (tempRepository.count() > 0 || jobRepository.count() > 0) {
            return;
        }

        Temp admin = new Temp();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("admin12345"));
        admin = tempRepository.save(admin);

        Temp allan = new Temp();
        allan.setFirstName("Allan");
        allan.setLastName("Taylor");
        allan.setEmail("allan@example.com");
        allan.setPasswordHash(passwordEncoder.encode("allan12345"));
        allan.setManager(admin);
        allan = tempRepository.save(allan);

        Temp sam = new Temp();
        sam.setFirstName("Sam");
        sam.setLastName("Nguyen");
        sam.setEmail("sam@example.com");
        sam.setPasswordHash(passwordEncoder.encode("sam12345"));
        sam.setManager(admin);
        sam = tempRepository.save(sam);

        Temp jamie = new Temp();
        jamie.setFirstName("Jamie");
        jamie.setLastName("Lopez");
        jamie.setEmail("jamie@example.com");
        jamie.setPasswordHash(passwordEncoder.encode("jamie12345"));
        jamie.setManager(allan);
        jamie = tempRepository.save(jamie);

        Job j1 = new Job();
        j1.setName("Warehouse shift");
        j1.setStartDate(LocalDate.now().plusDays(1));
        j1.setEndDate(LocalDate.now().plusDays(3));
        j1.setTemp(allan);
        jobRepository.save(j1);

        Job j2 = new Job();
        j2.setName("Site labouring");
        j2.setStartDate(LocalDate.now().plusDays(4));
        j2.setEndDate(LocalDate.now().plusDays(6));
        jobRepository.save(j2);

        Job j3 = new Job();
        j3.setName("Forklift cover");
        j3.setStartDate(LocalDate.now().plusDays(2));
        j3.setEndDate(LocalDate.now().plusDays(2));
        j3.setTemp(sam);
        jobRepository.save(j3);

        Job j4 = new Job();
        j4.setName("Packing support");
        j4.setStartDate(LocalDate.now().plusDays(7));
        j4.setEndDate(LocalDate.now().plusDays(8));
        j4.setTemp(jamie);
        jobRepository.save(j4);

           Job j5 = new Job();
        j5.setName("General labour");
        j5.setStartDate(LocalDate.now().plusDays(7));
        j5.setEndDate(LocalDate.now().plusDays(8));
        jobRepository.save(j5);
    }
}