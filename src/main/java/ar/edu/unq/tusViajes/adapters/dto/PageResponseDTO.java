package ar.edu.unq.tusViajes.adapters.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponseDTO<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public PageResponseDTO {
        if (content == null) {
            content = List.of();
        }
    }

    public static <T> PageResponseDTO<T> empty() {
        return new PageResponseDTO<>(List.of(), 0, 0, 0, 0);
    }

    public static <T> PageResponseDTO<T> of(List<T> content) {
        return new PageResponseDTO<>(content, content != null ? content.size() : 0, 1, content != null ? content.size() : 0, 0);
    }

    public Page<T> toPage() {
        int pageSize = size > 0 ? size : (content.isEmpty() ? 10 : content.size());
        int pageNumber = Math.max(0, number);
        return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);
    }
}
