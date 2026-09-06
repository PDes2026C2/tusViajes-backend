package ar.edu.unq.tusViajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hotel")
@Getter
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String destino;

    @Column(name = "foto_url", length = 300)
    private String fotoUrl;

    private String servicio;

    public Hotel(String nombre, String destino, 
                 String fotoUrl, String servicio) {
        this.nombre = nombre;
        this.destino = destino;
        this.fotoUrl = fotoUrl;
        this.servicio = servicio;
    }

}
