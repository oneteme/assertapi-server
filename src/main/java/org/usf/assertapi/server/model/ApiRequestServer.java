package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.usf.assertapi.core.ApiNonRegressionCheck;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiRequestServer {
    private final ApiNonRegressionCheck request;
    private List<ApiRequestGroupServer> requestGroupList;
}
