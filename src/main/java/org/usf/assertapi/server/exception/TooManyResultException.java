package org.usf.assertapi.server.exception;

@SuppressWarnings("serial")
public final class TooManyResultException extends RuntimeException {
	
    public TooManyResultException() {
        super("Too many results");
    }
}
