package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.PerfilAgencia;

public interface PerfilAgenciaRepository extends JpaRepository<PerfilAgencia, Long> {
  
    boolean existsByAgenciaId(Long agenciaId);
}
