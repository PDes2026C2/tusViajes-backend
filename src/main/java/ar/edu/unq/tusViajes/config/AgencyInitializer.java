package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AgencyInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AgencyInitializer.class);

    private final AgencyRepository agencyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (agencyRepository.count() == 0) {
            Agency agency = new Agency(
                    "agency@tusviajes.com",
                    passwordEncoder.encode("agency123"),
                    "Agency",
                    "30-40663891-2",
                    AgencyStatus.AUTHORIZED
            );
            agencyRepository.save(agency);
            logger.info("Initial agency successfully created: agency@tusviajes.com");
        }
    }
}
