package ar.edu.unq.tusViajes.exception;

public class FlightFullException extends RuntimeException {
    public FlightFullException(String message) {
        super(message);
    }
}
