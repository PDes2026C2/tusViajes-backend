package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "El token es obligatorio")
        String refreshToken
) {
}
