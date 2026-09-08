package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyRegistrationResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.InvalidCredentialsException;
import ar.edu.unq.tusViajes.exception.InvalidRefreshTokenException;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.User;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.UserRepository;
import ar.edu.unq.tusViajes.security.JwtTokenService;
import ar.edu.unq.tusViajes.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final BuyerService buyerService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserValidator userValidator;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new UnauthorizedAgencyException("The agency is pending authorization by an administrator.");
        }

        String token = jwtTokenService.generateToken(user);
        String refreshToken = jwtTokenService.generateToken(user, true);

        return new LoginResponseDTO(
                token,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getVisualIdentifier(),
                user.getRole().name()
        );
    }

    @Transactional
    public AgencyRegistrationResponseDTO registerAgency(AgencyRegistrationRequestDTO dto) {
        userValidator.validateEmailAvailable(dto.email());

        if (agencyRepository.existsByTaxId(dto.taxId())) {
            throw new DuplicateResourceException("An agency already exists with tax ID " + dto.taxId());
        }

        String hash = passwordEncoder.encode(dto.password());
        Agency agency = new Agency(dto.email(), hash, dto.businessName(), dto.taxId());
        Agency savedAgency = agencyRepository.save(agency);

        return AgencyRegistrationResponseDTO.from(
                savedAgency,
                "Registration request received. Pending authorization by an administrator."
        );
    }

    @Transactional
    public BuyerResponseDTO registerBuyer(BuyerRegistrationRequestDTO dto) {
        return buyerService.register(dto);
    }

    public LoginResponseDTO refreshToken(String token) {
        if (!jwtTokenService.isValid(token) || !jwtTokenService.isRefreshToken(token)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtTokenService.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        String newToken = jwtTokenService.generateToken(user);
        String refreshToken = jwtTokenService.generateToken(user, true);

        return new LoginResponseDTO(
                newToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getVisualIdentifier(),
                user.getRole().name()
        );
    }
}
