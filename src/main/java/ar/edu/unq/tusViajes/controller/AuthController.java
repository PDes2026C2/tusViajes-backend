package ar.edu.unq.tusViajes.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RefreshTokenRequest;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyRegistrationResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/register/agency")
    public ResponseEntity<AgencyRegistrationResponseDTO> registerAgency(@Valid @RequestBody AgencyRegistrationRequestDTO dto) {
        AgencyRegistrationResponseDTO response = authService.registerAgency(dto);
        return ResponseEntity.created(URI.create("/api/agencies/" + response.id())).body(response);
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody BuyerRegistrationRequestDTO dto) {
        BuyerResponseDTO response = authService.registerBuyer(dto);
        return ResponseEntity.created(URI.create("/api/buyers/" + response.id())).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest dto) {
        return ResponseEntity.ok(authService.refreshToken(dto.refreshToken()));
    }
}
