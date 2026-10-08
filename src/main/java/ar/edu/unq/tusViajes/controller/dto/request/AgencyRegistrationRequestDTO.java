package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AgencyRegistrationRequestDTO(
    @NotBlank(message = "La razón social es obligatoria")
    String businessName,

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{2}-\\d{8}-\\d{1}", message = "El CUIT debe seguir el formato XX-XXXXXXXX-X")
    String taxId,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser una dirección válida")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password
) {}
