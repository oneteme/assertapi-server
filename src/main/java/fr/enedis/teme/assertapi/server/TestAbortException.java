package fr.enedis.teme.assertapi.server;

@SuppressWarnings("serial")
public class TestAbortException extends RuntimeException {
	
	public TestAbortException() {
		super("Skipped test");
	}

}
