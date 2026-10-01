package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAgencyRequestDTO(
    @NotBlank(message = "La razón social es obligatoria") String businessName
) {}
