package org.usf.assertapi.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.usf.assertapi.core.ModelComparator;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonInclude(NON_NULL)
@RequiredArgsConstructor
public class ApiMigration {
    private final Integer id;
    private final Integer idReqOne;
    private final Integer idReqTwo;
    private final ModelComparator<?> contentComparator;
}
