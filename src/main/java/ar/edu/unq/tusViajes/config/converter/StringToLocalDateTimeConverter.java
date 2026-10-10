package ar.edu.unq.tusViajes.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String trimmed = source.trim();
        try {
            return LocalDateTime.parse(trimmed);
        } catch (DateTimeParseException e1) {
            try {
                return OffsetDateTime.parse(trimmed).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return LocalDate.parse(trimmed).atStartOfDay();
            }
        }
    }
}
