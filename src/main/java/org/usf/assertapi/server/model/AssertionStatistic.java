package org.usf.assertapi.server.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.usf.assertapi.core.ComparisonStatus.*;

import java.util.List;

import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.ComparisonStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
@AllArgsConstructor
public class AssertionStatistic {

    private final int nbTest;
    private int nbTestSkip;
    private int nbTestOk;
    private int nbTestError;
    private int nbTestFail;

    public void append(ComparisonStatus ts){
        if (ts == OK) {
            nbTestOk++;
        } else if (ts == ERROR) {
            nbTestError++;
        } else if (ts == FAIL) {
            nbTestFail++;
        } else if (ts == SKIP) {
            nbTestSkip++;
        }
    }
    
    public boolean isComplete() {
    	return nbTest == nbTestSkip + nbTestOk + nbTestError + nbTestFail;
	}
    
    public static final AssertionStatistic from(List<ApiRequest> reqList) {
    	return new AssertionStatistic(reqList.size());
    }
    
	public static final AssertionStatistic NO_STAT = new AssertionStatistic(0) {
		@Override
		public void append(ComparisonStatus ts) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean isComplete() {
			throw new UnsupportedOperationException();
		}
	};
}
