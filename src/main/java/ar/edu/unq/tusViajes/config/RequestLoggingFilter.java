package ar.edu.unq.tusViajes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator") || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = requestId(request.getHeader(REQUEST_ID_HEADER));
        String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long start = System.nanoTime();
        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        try {
            chain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            try {
                Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                Level level = status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO;
                logger.atLevel(level)
                        .addKeyValue("httpMethod", request.getMethod())
                        .addKeyValue("httpRoute", route == null ? "UNMAPPED" : route.toString())
                        .addKeyValue("httpStatus", status)
                        .addKeyValue("durationMs", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
                        .log("Solicitud HTTP finalizada");
            } finally {
                if (previousRequestId == null) {
                    MDC.remove(REQUEST_ID_MDC_KEY);
                } else {
                    MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
                }
            }
        }
    }

    private String requestId(String candidate) {
        if (candidate != null && candidate.length() == 36) {
            try {
                String normalized = UUID.fromString(candidate).toString();
                if (normalized.equalsIgnoreCase(candidate)) {
                    return normalized;
                }
            } catch (IllegalArgumentException ignored) {
                // Un identificador inválido se reemplaza sin registrar el valor recibido.
            }
        }
        return UUID.randomUUID().toString();
    }
}
