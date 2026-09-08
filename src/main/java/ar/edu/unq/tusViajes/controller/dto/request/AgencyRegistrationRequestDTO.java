package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AgencyRegistrationRequestDTO(
    @NotBlank(message = "Business name is required")
    String businessName,

    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "\\d{2}-\\d{8}-\\d{1}", message = "Tax ID must follow the format XX-XXXXXXXX-X")
    String taxId,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must have at least 8 characters")
    String password
) {}
