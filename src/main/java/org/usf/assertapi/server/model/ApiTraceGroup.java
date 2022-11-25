package org.usf.assertapi.server.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiTraceGroup {

    private final String user;
    private final String os;
    private final String address;
    private final String app;
    private final String actualEnv;
    private final String expectedEnv;
    private final int nbTest;
    private final int nbTestOk;
    private final int nbTestDisable;

}
