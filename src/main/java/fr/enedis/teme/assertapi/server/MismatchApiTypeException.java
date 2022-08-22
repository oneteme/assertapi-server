package fr.enedis.teme.assertapi.server;

@SuppressWarnings("serial")
public final class MismatchApiTypeException extends ApiAssertionsException {

	public MismatchApiTypeException() {
		super("Content Type");
	}

}
