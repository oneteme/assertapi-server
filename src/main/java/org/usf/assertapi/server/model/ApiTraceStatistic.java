package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.usf.assertapi.core.TestStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.usf.assertapi.core.TestStatus.*;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiTraceStatistic {

    private final int nbTest;
    private final int nbTestSkip;
    private int nbTestOk;
    private int nbTestKo;

    public void append(TestStatus ts){
        if (ts == OK) {
            nbTestOk++;
        } else if (ts == KO || ts == FAIL) {
            nbTestKo++;
        }
    }
}
