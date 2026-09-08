package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BuyerRegistrationRequestDTO(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address") String email,
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must have at least 8 characters") String password,
    String phoneNumber,
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "\\d{8}", message = "National ID must contain 8 numeric digits") String nationalId
) {}
