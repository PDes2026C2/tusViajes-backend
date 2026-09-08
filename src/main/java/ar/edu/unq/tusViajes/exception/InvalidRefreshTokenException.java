package ar.edu.unq.tusViajes.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token.");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
