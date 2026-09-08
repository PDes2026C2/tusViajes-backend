package ar.edu.unq.tusViajes.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("El refresh token no es valido.");
    }
}
