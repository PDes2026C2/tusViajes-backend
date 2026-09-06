package ar.edu.unq.tusViajes.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaqueteResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private HotelResponseDTO hotel;
    private AgenciaResponseDTO agencia;
}
