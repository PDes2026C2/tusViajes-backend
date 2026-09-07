package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompradorRequestDTO(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El apellido es obligatorio") String apellido,
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido") String email,
    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres") String password,
    String telefono,
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener 8 digitos numericos") String dni
) {}
