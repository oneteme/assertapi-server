package org.usf.assertapi.server.exception;

import org.usf.assertapi.server.exception.ApiAssertionsException;

@SuppressWarnings("serial")
public final class TestAbortException extends ApiAssertionsException {
	
	public TestAbortException() {
		super("Skipped test");
	}

}
