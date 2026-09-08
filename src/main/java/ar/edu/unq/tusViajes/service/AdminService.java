package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.AdminRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AdminResponseDTO;
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
    public List<AdminResponseDTO> getAll() {
        return adminRepository.findAll().stream()
                .map(AdminResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminResponseDTO getById(Long id) {
        return AdminResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public AdminResponseDTO create(AdminRegistrationRequestDTO dto) {
        userValidator.validateEmailAvailable(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        Admin admin = new Admin(dto.firstName(), dto.lastName(), dto.email(), hash);
        return AdminResponseDTO.from(adminRepository.save(admin));
    }

    public Admin getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(adminRepository, id, "Admin");
    }
}
