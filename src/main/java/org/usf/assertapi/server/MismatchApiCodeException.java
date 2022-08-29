package org.usf.assertapi.server;

@SuppressWarnings("serial")
public final class MismatchApiCodeException extends ApiAssertionsException {

	public MismatchApiCodeException() {
		super("Status code");
	}

}
