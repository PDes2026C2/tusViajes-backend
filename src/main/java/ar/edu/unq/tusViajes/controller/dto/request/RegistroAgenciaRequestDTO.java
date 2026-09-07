package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroAgenciaRequestDTO(
    @NotBlank(message = "La razon social es obligatoria")
    String razonSocial,

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{2}-\\d{8}-\\d{1}", message = "El CUIT debe tener el formato XX-XXXXXXXX-X")
    String cuit,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    String email,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    String password
) {}
