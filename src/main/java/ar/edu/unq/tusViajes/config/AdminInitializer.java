package ar.edu.unq.tusViajes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ar.edu.unq.tusViajes.model.Admin;
import org.springframework.context.annotation.Profile;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin(
                    "Admin",
                    "Sistema",
                    "admin@tusviajes.com",
                    passwordEncoder.encode("admin123")
            );
            adminRepository.save(admin);
            logger.info("Administrador inicial creado exitosamente: admin@tusviajes.com");
        }
    }
}
