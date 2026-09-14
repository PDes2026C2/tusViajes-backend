package ar.edu.unq.tusViajes.repository;

import ar.edu.unq.tusViajes.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
}
