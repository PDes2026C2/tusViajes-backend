package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAgenciaRequestDTO(
    @NotBlank(message = "La razon social es obligatoria") String razonSocial
) {}