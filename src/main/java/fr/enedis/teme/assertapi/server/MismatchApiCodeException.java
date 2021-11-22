package fr.enedis.teme.assertapi.server;

@SuppressWarnings("serial")
public final class MismatchApiCodeException extends ApiAssertionsException {

	public MismatchApiCodeException() {
		super("Status code");
	}

}
