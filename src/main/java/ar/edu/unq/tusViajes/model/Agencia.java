package ar.edu.unq.tusViajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agencia")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agencia extends Usuario {

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 13, unique = true)
    private String cuit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAgencia estado = EstadoAgencia.PENDIENTE;

    public Agencia(String email, String passwordHash, String razonSocial, String cuit) {
        super(email, passwordHash);
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.estado = EstadoAgencia.PENDIENTE;
    }

    public Agencia(String email, String passwordHash, String razonSocial, String cuit, EstadoAgencia estado) {
        super(email, passwordHash);
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.estado = estado;
    }

    public void actualizarRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public void autorizar() {
        this.estado = EstadoAgencia.AUTORIZADA;
    }

    public void rechazar() {
        this.estado = EstadoAgencia.RECHAZADA;
    }

    public boolean isAutorizada() {
        return EstadoAgencia.AUTORIZADA.equals(this.estado);
    }

    @Override
    public Rol getRol() {
        return Rol.AGENCIA;
    }

    @Override
    public boolean isActivo() {
        return isAutorizada();
    }

    @Override
    public String getIdentificadorVisual() {
        return razonSocial;
    }
}
