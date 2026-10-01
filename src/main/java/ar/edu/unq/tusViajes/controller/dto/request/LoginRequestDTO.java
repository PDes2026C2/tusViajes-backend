package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser una dirección válida")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
