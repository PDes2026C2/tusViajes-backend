package ar.edu.unq.tusViajes.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales invalidas");
    }
}
