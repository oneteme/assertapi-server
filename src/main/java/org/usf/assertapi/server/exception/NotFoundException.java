package org.usf.assertapi.server.exception;

@SuppressWarnings("serial")
public final class NotFoundException extends RuntimeException {
	
    public NotFoundException() {
        super("not found");
    }
}
