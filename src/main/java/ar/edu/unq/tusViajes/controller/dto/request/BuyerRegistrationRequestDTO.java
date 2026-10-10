package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BuyerRegistrationRequestDTO(
    @NotBlank(message = "El nombre es obligatorio") String firstName,
    @NotBlank(message = "El apellido es obligatorio") String lastName,
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser una dirección válida") String email,
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
    String phoneNumber,
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]$", message = "El DNI debe contener solo dígitos numéricos") String nationalId
) {}
