package ar.edu.unq.tusViajes.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_returns404WithErrorDTO() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Package not found");

        ResponseEntity<ErrorDTO> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().code());
        assertEquals("Package not found", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleDuplicate_returns409WithErrorDTO() {
        DuplicateResourceException ex = new DuplicateResourceException("User already exists");

        ResponseEntity<ErrorDTO> response = handler.handleDuplicate(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().code());
        assertEquals("User already exists", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleUnauthorizedAgency_returns403WithErrorDTO() {
        UnauthorizedAgencyException ex = new UnauthorizedAgencyException("Agency not authorized");

        ResponseEntity<ErrorDTO> response = handler.handleUnauthorizedAgency(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().code());
        assertEquals("Agency not authorized", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleReviewNotAllowed_returns403WithErrorDTO() {
        ReviewNotAllowedException ex = new ReviewNotAllowedException("Only buyers can review");

        ResponseEntity<ErrorDTO> response = handler.handleReviewNotAllowed(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().code());
        assertEquals("Only buyers can review", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleInvalidCredentials_returns401WithErrorDTO() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<ErrorDTO> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().code());
        assertEquals("Invalid credentials", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleInvalidRefreshToken_returns401WithErrorDTO() {
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException();

        ResponseEntity<ErrorDTO> response = handler.handleInvalidRefreshToken(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().code());
        assertEquals("Invalid refresh token.", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleValidation_returns400WithErrorsMapInErrorDTO() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("dto", "email", "must not be blank");
        FieldError fieldError2 = new FieldError("dto", "password", "size must be at least 8");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorDTO> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().code());
        assertEquals("Invalid data", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNotNull(response.getBody().errors());
        assertEquals("must not be blank", response.getBody().errors().get("email"));
        assertEquals("size must be at least 8", response.getBody().errors().get("password"));
    }

    @Test
    void handleNotReadable_returns400WithErrorDTO() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorDTO> response = handler.handleNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().code());
        assertEquals("Malformed request payload", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }

    @Test
    void handleGenericException_returns500WithErrorDTO() {
        Exception ex = new RuntimeException("Unexpected db crash");

        ResponseEntity<ErrorDTO> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().code());
        assertEquals("Internal server error", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().errors());
    }
}
