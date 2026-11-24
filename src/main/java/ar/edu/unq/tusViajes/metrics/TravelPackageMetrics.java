package ar.edu.unq.tusViajes.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TravelPackageMetrics {

    private static final String SEARCHES = "tusviajes.package.searches";
    private static final String DESTINATION_SEARCHES = "tusviajes.package.destination.searches";
    private static final Pattern COUNTRY_CODE = Pattern.compile("[A-Za-z]{2}");

    private final Counter found;
    private final Counter empty;
    private final Map<String, Counter> countries;

    public TravelPackageMetrics(MeterRegistry registry) {
        found = registry.counter(SEARCHES, "result", "found");
        empty = registry.counter(SEARCHES, "result", "empty");

        // Evita que la primera muestra de una serie ya incluya su primer incremento.
        countries = Arrays.stream(Locale.getISOCountries()).collect(Collectors.toUnmodifiableMap(
                Function.identity(),
                code -> registry.counter(DESTINATION_SEARCHES, "type", "country", "destination", code)));
    }

    public void recordSearch(boolean hasResults, String destinationCountryIso) {
        (hasResults ? found : empty).increment();
        recordCountry(destinationCountryIso);
    }

    private void recordCountry(String destinationCountryIso) {
        if (destinationCountryIso == null) {
            return;
        }
        String code = destinationCountryIso.trim();
        if (!COUNTRY_CODE.matcher(code).matches()) {
            return;
        }
        Counter counter = countries.get(code.toUpperCase(Locale.ROOT));
        if (counter != null) {
            counter.increment();
        }
    }
}
