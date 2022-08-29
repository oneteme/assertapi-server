package org.usf.assertapi.server;

@SuppressWarnings("serial")
public final class MismatchApiTypeException extends ApiAssertionsException {

	public MismatchApiTypeException() {
		super("Content Type");
	}

}
