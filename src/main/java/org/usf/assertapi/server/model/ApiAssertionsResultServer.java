package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.core.ApiRequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class ApiAssertionsResultServer {
    private final ApiAssertionsResult result;
    private final ApiRequest request;
}
