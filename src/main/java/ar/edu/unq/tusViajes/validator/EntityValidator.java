package ar.edu.unq.tusViajes.validator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;

@Component
public class EntityValidator {

    public <T, ID> T findByIdOrThrow(
            JpaRepository<T, ID> repository,
            ID id,
            String nombreEntidad
    ) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                nombreEntidad + " " + id + " no encontrado"
                        )
                );
    }
}