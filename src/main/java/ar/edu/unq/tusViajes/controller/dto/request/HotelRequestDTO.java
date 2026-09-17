package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotelRequestDTO(
    @NotBlank(message = "Name is required") String name,
    @NotNull(message = "City is required") Long cityId,
    String photoUrl,
    String services
) {}
