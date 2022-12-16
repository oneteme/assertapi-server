package org.usf.assertapi.server.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.usf.assertapi.core.TestStatus.FAIL;
import static org.usf.assertapi.core.TestStatus.KO;
import static org.usf.assertapi.core.TestStatus.OK;

import java.util.List;

import org.usf.assertapi.core.ApiRequest;
import org.usf.assertapi.core.TestStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    
    public boolean isComplete() {
    	return nbTest == nbTestSkip + nbTestOk + nbTestKo;
	}
    
    public static final ApiTraceStatistic from(List<ApiRequest> reqList) {
    	return new ApiTraceStatistic(reqList.size(), (int)reqList.stream().filter(l -> !l.getConfiguration().isEnable()).count());
    }
    
	public static final ApiTraceStatistic NO_STAT = new ApiTraceStatistic(0, 0) {
		@Override
		public void append(TestStatus ts) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean isComplete() {
			throw new UnsupportedOperationException();
		}
	};
}
