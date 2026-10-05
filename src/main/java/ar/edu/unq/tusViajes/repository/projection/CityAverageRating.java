package ar.edu.unq.tusViajes.repository.projection;

import ar.edu.unq.tusViajes.model.City;

public interface CityAverageRating {
    City getCity();
    Double getStars();
}
