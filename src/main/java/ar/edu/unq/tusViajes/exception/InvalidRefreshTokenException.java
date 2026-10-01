package ar.edu.unq.tusViajes.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Token de refresco inválido.");
    }

}
