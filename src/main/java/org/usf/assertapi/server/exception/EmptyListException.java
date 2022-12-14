package org.usf.assertapi.server.exception;

public class EmptyListException extends RuntimeException {
    public EmptyListException() {
        super("Empty list");
    }
}
