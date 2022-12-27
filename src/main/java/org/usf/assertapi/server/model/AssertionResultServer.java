package org.usf.assertapi.server.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.HttpRequest;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class AssertionResultServer {
	
    private final ComparisonResult result;
    private final HttpRequest request;
}
