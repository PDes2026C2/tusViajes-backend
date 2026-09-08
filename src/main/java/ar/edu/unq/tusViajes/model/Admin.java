package ar.edu.unq.tusViajes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends Usuario {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    public Admin(String nombre, String apellido, String email, String passwordHash) {
        super(email, passwordHash);
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public void actualizarDatos(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    @Override
    public Rol getRol() {
        return Rol.ADMIN;
    }

    @Override
    public String getIdentificadorVisual() {
        return nombre + " " + apellido;
    }
}
