package ar.edu.unq.tusViajes.exception;

public class ReviewNotAllowedException extends RuntimeException {

    public ReviewNotAllowedException() {
        super("A travel package must be a favorite to be reviewed");
    }
}