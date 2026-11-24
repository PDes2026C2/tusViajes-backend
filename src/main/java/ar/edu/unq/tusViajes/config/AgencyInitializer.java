package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("!test")
@Order(1)
public class AgencyInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AgencyInitializer.class);

    private final AgencyRepository agencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialPassword;

    public AgencyInitializer(
            AgencyRepository agencyRepository,
            PasswordEncoder passwordEncoder,
            @Value("${TUSVIAJES_BOOTSTRAP_AGENCY_PASSWORD:}") String initialPassword) {
        this.agencyRepository = agencyRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(initialPassword) || agencyRepository.count() > 0) {
            return;
        }

        Agency agency = new Agency(
                "agency@tusviajes.com",
                passwordEncoder.encode(initialPassword),
                "Agency",
                "30-40663891-2",
                AgencyStatus.AUTHORIZED
        );
        agencyRepository.save(agency);
        logger.info("Initial agency successfully created: agency@tusviajes.com");
    }
}
