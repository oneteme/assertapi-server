package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.usf.assertapi.core.ComparisonStatus;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class ApiTrace {
    private final Long requestId;
    private final String requestName;
    private final String requestDescription;
    private final String requestMethod;
    private final String requestUri;
    private final Map<String, List<String>> requestHeaders;
    private final String requestBody;
    private final ComparisonStatus assertionStatus;
}
