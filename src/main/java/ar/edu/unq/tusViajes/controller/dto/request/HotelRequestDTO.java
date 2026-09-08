package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HotelRequestDTO(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Destination is required") String destination,
    String photoUrl,
    String services
) {}
