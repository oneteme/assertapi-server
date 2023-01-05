package org.usf.assertapi.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.usf.assertapi.core.ServerConfig;

@Getter
@RequiredArgsConstructor
public class ApiEnvironment {
    private final Long id;
    private final ServerConfig serverConfig;
    private final String app;
    private final String env;
    private final boolean isProd;
}
