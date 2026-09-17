package ar.edu.unq.tusViajes.repository;

import ar.edu.unq.tusViajes.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountry_IsoCodeIgnoreCase(String isoCode);
}

