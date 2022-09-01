package org.usf.assertapi.server.exception;

import org.usf.assertapi.server.exception.ApiAssertionsException;

@SuppressWarnings("serial")
public final class MismatchApiCodeException extends ApiAssertionsException {

	public MismatchApiCodeException() {
		super("Status code");
	}

}
