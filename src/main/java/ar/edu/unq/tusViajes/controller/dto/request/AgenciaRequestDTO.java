package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AgenciaRequestDTO(
    @NotBlank(message = "La razon social es obligatoria") String razonSocial,
    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "\\d{2}-\\d{8}-\\d{1}", message = "El CUIT debe tener el formato XX-XXXXXXXX-X") String cuit
) {}