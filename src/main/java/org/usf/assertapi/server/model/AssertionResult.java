package org.usf.assertapi.server.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import lombok.*;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ComparisonResult;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
@AllArgsConstructor
public class AssertionResult {
    private final Long id;
    private final String name;
    private final String description;
    private final String uri;
    private final String method;
    private ComparisonResult result;

    public static final AssertionResult from(ApiRequest apiRequest) {
        return new AssertionResult(apiRequest.getId(), apiRequest.getName(), apiRequest.getDescription(), apiRequest.getUri(), apiRequest.getMethod());
    }
}
