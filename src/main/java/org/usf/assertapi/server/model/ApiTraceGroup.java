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
    private final String jre;
    private final String branch;
    private final String app;
    private final String stableRelease;
    private final String latestRelease;
    private final ExecutionState status;

    public ApiTraceGroup(Long id, String user, String os, String address, String jre, String branch, String app, String stableRelease, String latestRelease, ExecutionState status, int nbTest, int nbTestSkip) {
        super(nbTest, nbTestSkip);
        this.id = id;
        this.user = user;
        this.os = os;
        this.address = address;
        this.jre = jre;
        this.branch = branch;
        this.app = app;
        this.stableRelease = stableRelease;
        this.latestRelease = latestRelease;
        this.status = status;
    }

    public ApiTraceGroup(Long id, String user, String os, String address, String jre, String branch, String app, String stableRelease, String latestRelease, ExecutionState status, int nbTest, int nbTestSkip, int nbTestOk, int nbTestKo) {
        super(nbTest, nbTestSkip, nbTestOk, nbTestKo);
        this.id = id;
        this.user = user;
        this.os = os;
        this.address = address;
        this.jre = jre;
        this.branch = branch;
        this.app = app;
        this.stableRelease = stableRelease;
        this.latestRelease = latestRelease;
        this.status = status;
    }
}
