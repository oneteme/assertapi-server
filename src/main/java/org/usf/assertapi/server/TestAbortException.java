package org.usf.assertapi.server;

@SuppressWarnings("serial")
public final class TestAbortException extends ApiAssertionsException {
	
	public TestAbortException() {
		super("Skipped test");
	}

}
