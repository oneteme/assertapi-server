package org.usf.assertapi.server.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.AssertionResult;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class AssertionResultServer {
    private final AssertionResult result;
    private final ApiRequest request;
}
