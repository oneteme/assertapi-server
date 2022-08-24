package org.usf.assertapi.server;

@SuppressWarnings("serial")
public class ApiAssertionsException extends RuntimeException {

	public ApiAssertionsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiAssertionsException(String message) {
		super(message);
	}

}
