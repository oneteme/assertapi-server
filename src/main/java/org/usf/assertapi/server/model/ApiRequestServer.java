package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.usf.assertapi.core.ApiRequest;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public final class ApiRequestServer {
    private final ApiRequest request;
    private final Map<String, String> metadata;
}
