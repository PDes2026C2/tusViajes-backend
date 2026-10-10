package ar.edu.unq.tusViajes.specification;

import ar.edu.unq.tusViajes.model.TravelPackage;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class TravelPackageSpecificationsTest {

    @Test
    void privateConstructor_canBeInvokedViaReflection() throws Exception {
        Constructor<TravelPackageSpecifications> constructor = TravelPackageSpecifications.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TravelPackageSpecifications instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }

    @Test
    void fromFilter_whenFilterIsNull_returnsIsActiveSpecification() {
        Specification<TravelPackage> spec = TravelPackageSpecifications.fromFilter(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void originCountryIs_whenNullOrBlank_returnsNull() {
        assertThat(TravelPackageSpecifications.originCountryIs(null)).isNull();
        assertThat(TravelPackageSpecifications.originCountryIs("")).isNull();
        assertThat(TravelPackageSpecifications.originCountryIs("   ")).isNull();
    }

    @Test
    void originCityIdIs_whenNullOrZeroOrNegative_returnsNull() {
        assertThat(TravelPackageSpecifications.originCityIdIs(null)).isNull();
        assertThat(TravelPackageSpecifications.originCityIdIs(0L)).isNull();
        assertThat(TravelPackageSpecifications.originCityIdIs(-1L)).isNull();
    }

    @Test
    void destinationCountryIs_whenNullOrBlank_returnsNull() {
        assertThat(TravelPackageSpecifications.destinationCountryIs(null)).isNull();
        assertThat(TravelPackageSpecifications.destinationCountryIs("")).isNull();
        assertThat(TravelPackageSpecifications.destinationCountryIs("   ")).isNull();
    }

    @Test
    void destinationCityIdIs_whenNullOrZeroOrNegative_returnsNull() {
        assertThat(TravelPackageSpecifications.destinationCityIdIs(null)).isNull();
        assertThat(TravelPackageSpecifications.destinationCityIdIs(0L)).isNull();
        assertThat(TravelPackageSpecifications.destinationCityIdIs(-1L)).isNull();
    }

    @Test
    void dateSpecifications_whenNull_returnNull() {
        assertThat(TravelPackageSpecifications.departureAfter(null)).isNull();
        assertThat(TravelPackageSpecifications.departureBefore(null)).isNull();
        assertThat(TravelPackageSpecifications.arrivalAfter(null)).isNull();
        assertThat(TravelPackageSpecifications.arrivalBefore(null)).isNull();
    }
}
