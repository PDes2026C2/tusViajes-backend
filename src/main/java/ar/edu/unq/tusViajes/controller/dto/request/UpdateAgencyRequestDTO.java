package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAgencyRequestDTO(
    @NotBlank(message = "Business name is required") String businessName
) {}
