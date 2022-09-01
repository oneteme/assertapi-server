package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.usf.assertapi.core.ApiRequest;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@JsonInclude(NON_NULL)
public final class ApiRequestServer {
    private final ApiRequest request;
    private final Map<String, String> metadata;

    public ApiRequestServer(ApiRequest request, Map<String, String> metadata) {
        this.request = request;
        this.metadata = metadata;
    }
}
