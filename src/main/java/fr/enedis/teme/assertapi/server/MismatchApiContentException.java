package fr.enedis.teme.assertapi.server;

@SuppressWarnings("serial")
public final class MismatchApiContentException extends ApiAssertionsException {

	public MismatchApiContentException() {
		super("Response content");
	}

	public MismatchApiContentException(String message) {
		super(message);
	}

}
