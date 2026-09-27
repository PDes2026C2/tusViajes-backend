package ar.edu.unq.tusViajes.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.AdminRegistrationRequestDTO;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UserValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final EntityValidator entityValidator;
    private final UserValidator userValidator;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Admin getById(Long id) {
        return getEntityById(id);
    }

    @Transactional
    public Admin create(AdminRegistrationRequestDTO dto) {
        userValidator.validateEmailAvailable(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        Admin admin = new Admin(dto.firstName(), dto.lastName(), dto.email(), hash);
        return adminRepository.save(admin);
    }

    public Admin getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(adminRepository, id, "Admin");
    }
}
