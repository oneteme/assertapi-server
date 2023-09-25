package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.usf.assertapi.core.ServerConfig;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class Environment {
    private final Long id;
    private final String app;
    private final String release;
    private final boolean isProd;
    private final ServerConfig serverConfig;
}
