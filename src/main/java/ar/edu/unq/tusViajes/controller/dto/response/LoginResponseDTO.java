package ar.edu.unq.tusViajes.controller.dto.response;

public record LoginResponseDTO(
    String token,
    String refreshToken,
    String tokenType,
    Long id,
    String email,
    String name,
    String role
) {}
