package org.usf.assertapi.server.exception;

public class TooManyListException extends RuntimeException {
    public TooManyListException() {
        super("Too many elements");
    }
}
