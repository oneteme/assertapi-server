package org.usf.assertapi.server.exception;

import org.usf.assertapi.server.exception.ApiAssertionsException;

@SuppressWarnings("serial")
public final class MismatchApiContentException extends ApiAssertionsException {

	public MismatchApiContentException() {
		super("Response content");
	}

	public MismatchApiContentException(String message) {
		super(message);
	}

}
