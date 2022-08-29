package org.usf.assertapi.server;

import java.util.Arrays;
import java.util.Objects;

import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.http.MediaType;
import org.usf.assertapi.core.ResponseComparator;

public final class DefaultResponseComparator implements ResponseComparator {

	@Override
	public void assumeEnabled(boolean enable) {
		if(!enable) {
			throw new TestAbortException();
		}
	}

	@Override
	public void assertStatusCode(int expectedStatusCode, int actualStatusCode) {
		if(expectedStatusCode != actualStatusCode) {
			throw new MismatchApiCodeException();
		}
	}

	@Override
	public void assertContentType(MediaType expectedContentType, MediaType actualContentType) {
		if(!Objects.equals(expectedContentType, actualContentType)) {
			throw new MismatchApiTypeException();
		}
	}

	@Override
	public void assertByteContent(byte[] expectedContent, byte[] actualContent) {
		if(!Arrays.equals(expectedContent, actualContent)) {
			throw new MismatchApiContentException();
		}
	}

	@Override
	public void assertTextContent(String expectedContent, String actualContent) {
		if(!Objects.equals(expectedContent, actualContent)) {
			throw new MismatchApiContentException();
		}
	}

	@Override
	public void assertJsonCompareResut(JSONCompareResult res) {
		if(res.failed()) {
			throw new MismatchApiContentException("Response content " + res.getMessage());
		}
	}
	
	@Override
	public void assertionFail(Throwable t) {
		throw new ApiAssertionFailException(t);
	}
	
}
