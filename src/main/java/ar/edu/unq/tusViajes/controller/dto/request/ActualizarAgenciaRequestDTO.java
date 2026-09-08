package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ActualizarAgenciaRequestDTO(
    @NotBlank(message = "La razon social es obligatoria") String razonSocial
) {}