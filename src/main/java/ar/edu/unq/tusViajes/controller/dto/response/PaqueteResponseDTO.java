package ar.edu.unq.tusViajes.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import ar.edu.unq.tusViajes.model.Paquete;

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

    public static PaqueteResponseDTO from(Paquete paquete) {
        if (paquete == null) {
            return null;
        }
        return new PaqueteResponseDTO(
                paquete.getId(),
                paquete.getNombre(),
                paquete.getDescripcion(),
                paquete.getPrecio(),
                paquete.getFechaInicio(),
                paquete.getFechaFin(),
                HotelResponseDTO.from(paquete.getHotel()),
                AgenciaResponseDTO.from(paquete.getAgencia())
        );
    }
}
