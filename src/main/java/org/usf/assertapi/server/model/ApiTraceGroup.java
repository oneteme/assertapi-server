package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@JsonInclude(NON_NULL)
public class ApiTraceGroup extends ApiTraceStatistic {
    private final Long id;
    private final String user;
    private final String os;
    private final String address;
    private final String app;
    private final String actualEnv;
    private final String expectedEnv;
    private final TraceGroupStatus status;

    public ApiTraceGroup(Long id, String user, String os, String address, String app, String actualEnv, String expectedEnv, TraceGroupStatus status, int nbTest, int nbTestSkip) {
        super(nbTest, nbTestSkip);
        this.id = id;
        this.user = user;
        this.os = os;
        this.address = address;
        this.app = app;
        this.actualEnv = actualEnv;
        this.expectedEnv = expectedEnv;
        this.status = status;
    }

    public ApiTraceGroup(Long id, String user, String os, String address, String app, String actualEnv, String expectedEnv, TraceGroupStatus status, int nbTest, int nbTestSkip, int nbTestOk, int nbTestKo) {
        super(nbTest, nbTestSkip, nbTestOk, nbTestKo);
        this.id = id;
        this.user = user;
        this.os = os;
        this.address = address;
        this.app = app;
        this.actualEnv = actualEnv;
        this.expectedEnv = expectedEnv;
        this.status = status;
    }
}
