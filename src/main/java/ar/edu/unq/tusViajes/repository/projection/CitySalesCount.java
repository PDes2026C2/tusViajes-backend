package ar.edu.unq.tusViajes.repository.projection;

import ar.edu.unq.tusViajes.model.City;

public interface CitySalesCount {
    City getCity();
    Long getSalesCount();
}
