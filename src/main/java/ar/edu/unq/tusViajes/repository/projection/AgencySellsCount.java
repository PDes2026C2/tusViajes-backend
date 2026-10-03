package ar.edu.unq.tusViajes.repository.projection;

import ar.edu.unq.tusViajes.model.Agency;

public interface AgencySellsCount {
    Agency getAgency();
    Long getSellsCount();
}
