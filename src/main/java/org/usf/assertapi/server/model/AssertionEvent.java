package org.usf.assertapi.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ComparisonResult;
import org.usf.assertapi.core.ComparisonStatus;

import java.util.List;

import static org.usf.assertapi.core.ComparisonStatus.*;
import static org.usf.assertapi.core.ComparisonStatus.SKIP;

@Getter
@Setter
@RequiredArgsConstructor
public class AssertionEvent {

    private final int nbTest;
    private int nbTestSkip;
    private int nbTestOk;
    private int nbTestError;
    private int nbTestFail;
    private AssertionResult assertionResult;

    public void append(AssertionResult result){
        var ts = result.getResult().getStatus();
        if (ts == OK) {
            nbTestOk++;
        } else if (ts == ERROR) {
            nbTestError++;
        } else if (ts == FAIL) {
            nbTestFail++;
        } else if (ts == SKIP) {
            nbTestSkip++;
        }
        setAssertionResult(result);
    }

    public boolean isComplete() {
        return nbTest == nbTestSkip + nbTestOk + nbTestError + nbTestFail;
    }

    public static final AssertionEvent from(List<ApiRequest> reqList) {
        return new AssertionEvent(reqList.size());
    }
}
