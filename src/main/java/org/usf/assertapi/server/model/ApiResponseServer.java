package org.usf.assertapi.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ApiResponseServer {
    private int statusCode;
    private String contentType;
    private String response;
}
