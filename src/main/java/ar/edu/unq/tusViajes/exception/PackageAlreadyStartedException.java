package ar.edu.unq.tusViajes.exception;

public class PackageAlreadyStartedException extends RuntimeException {
    public PackageAlreadyStartedException(String message) {
        super(message);
    }
}
