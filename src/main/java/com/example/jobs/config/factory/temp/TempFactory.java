package com.example.jobs.config.factory.temp;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.jobs.config.factory.BaseFactory;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;

@Component
@Profile("dev")
public class TempFactory extends BaseFactory {

    private final TempRepository tempRepository;
    private final PasswordEncoder passwordEncoder;

    public TempFactory(TempRepository tempRepository, PasswordEncoder passwordEncoder) {
        this.tempRepository = tempRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean repoEmpty() {
        return tempRepository.count() == 0;
    }

    public Temp create(TempFactoryOptions options) {
        Temp temp = new Temp();

        temp.setFirstName(options.firstName != null ? options.firstName : faker().name().firstName());
        temp.setLastName(options.lastName != null ? options.lastName : faker().name().lastName());
        temp.setEmail(options.email != null ? options.email : generateUniqueEmail());
        temp.setPasswordHash(passwordEncoder.encode(
                options.rawPassword != null ? options.rawPassword : "password12345"
        ));
        temp.setManager(options.manager);

        return temp;
    }

    public Temp create() {
        return create(new TempFactoryOptions());
    }

    public Temp createAndPersist(TempFactoryOptions options) {
        return tempRepository.save(create(options));
    }

    public Temp createAndPersist() {
        return createAndPersist(new TempFactoryOptions());
    }

    private String generateUniqueEmail() {
        String localPart = faker().internet().emailAddress().split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();

        if (localPart.isBlank()) {
            localPart = "temp";
        }

        return localPart + "_" + incrementAndGet() + "@example.com";
    }
}