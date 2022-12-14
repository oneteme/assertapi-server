package org.usf.assertapi.server.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiRequestGroupServer {
    private final String app;
    private final String env;
}
