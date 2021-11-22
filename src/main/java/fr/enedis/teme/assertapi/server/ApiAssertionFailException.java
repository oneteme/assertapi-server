package fr.enedis.teme.assertapi.server;

@SuppressWarnings("serial")
public final class ApiAssertionFailException extends ApiAssertionsException {

	public ApiAssertionFailException(Throwable cause) {
		super("Assertion fail", cause);
	}

}
