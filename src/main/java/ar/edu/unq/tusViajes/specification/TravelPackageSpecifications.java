package ar.edu.unq.tusViajes.specification;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageFilterDTO;
import ar.edu.unq.tusViajes.model.*;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class TravelPackageSpecifications {

    private TravelPackageSpecifications() {
    }

    public static Specification<TravelPackage> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<TravelPackage> originCountryIs(String isoCode) {
        if (!StringUtils.hasText(isoCode)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
            Join<Flight, City> city = getOrCreateJoin(flight, "originCity", JoinType.INNER);
            Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
            return cb.equal(cb.upper(country.get("isoCode")), isoCode.trim().toUpperCase());
        };
    }

    public static Specification<TravelPackage> originCityIdIs(Long cityId) {
        if (cityId == null || cityId <= 0) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Flight> flight = getOrCreateJoin(root, "departureFlight", JoinType.INNER);
            return cb.equal(flight.get("originCity").get("id"), cityId);
        };
    }

    public static Specification<TravelPackage> destinationCountryIs(String isoCode) {
        if (!StringUtils.hasText(isoCode)) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
            Join<Hotel, City> city = getOrCreateJoin(hotel, "city", JoinType.INNER);
            Join<City, Country> country = getOrCreateJoin(city, "country", JoinType.INNER);
            return cb.equal(cb.upper(country.get("isoCode")), isoCode.trim().toUpperCase());
        };
    }

    public static Specification<TravelPackage> destinationCityIdIs(Long cityId) {
        if (cityId == null || cityId <= 0) {
            return null;
        }
        return (root, query, cb) -> {
            Join<TravelPackage, Hotel> hotel = getOrCreateJoin(root, "hotel", JoinType.INNER);
            return cb.equal(hotel.get("city").get("id"), cityId);
        };
    }

    public static Specification<TravelPackage> departureAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    public static Specification<TravelPackage> departureBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), to);
    }

    public static Specification<TravelPackage> arrivalAfter(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), from);
    }

    public static Specification<TravelPackage> arrivalBefore(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), to);
    }

    public static Specification<TravelPackage> fromFilter(TravelPackageFilterDTO filter) {
        if (filter == null) {
            return isActive();
        }

        Specification<TravelPackage> spec = isActive();
        spec = andIfPresent(spec, originCountryIs(filter.originCountryIso()));
        spec = andIfPresent(spec, originCityIdIs(filter.originCityId()));
        spec = andIfPresent(spec, destinationCountryIs(filter.destinationCountryIso()));
        spec = andIfPresent(spec, destinationCityIdIs(filter.destinationCityId()));
        spec = andIfPresent(spec, departureAfter(filter.departureFrom()));
        spec = andIfPresent(spec, departureBefore(filter.departureTo()));
        spec = andIfPresent(spec, arrivalAfter(filter.arrivalFrom()));
        spec = andIfPresent(spec, arrivalBefore(filter.arrivalTo()));
        return spec;
    }

    private static Specification<TravelPackage> andIfPresent(Specification<TravelPackage> base, Specification<TravelPackage> other) {
        return other != null ? base.and(other) : base;
    }

    @SuppressWarnings("unchecked")
    private static <X, Y> Join<X, Y> getOrCreateJoin(From<?, X> from, String attributeName, JoinType joinType) {
        for (Join<X, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(attributeName)) {
                return (Join<X, Y>) join;
            }
        }
        return from.join(attributeName, joinType);
    }
}
