package org.usf.assertapi.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.usf.assertapi.core.ApiAssertionsResult;
import org.usf.assertapi.server.dao.TraceDao;
import org.usf.assertapi.server.model.ApiAssertionsResultServer;

import java.util.List;

import static org.usf.assertapi.core.AssertionContext.*;

@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assert/api/trace")
public class TraceController {
    private final TraceDao dao;
    private final ObjectMapper mapper;

    @GetMapping
    public List<ApiAssertionsResultServer> get(
            @RequestParam(name="id", required = false) long[] ids
    ) {
        return dao.select(ids);
    }

    @PutMapping
    public void put(@RequestHeader(CTX_ID) long ctx, @RequestBody ApiAssertionsResult result) {
        dao.insert(ctx, result);
    }

    @GetMapping("register")
    public long register(@RequestHeader(CTX) String context) {
        return dao.register(parseHeader(mapper, context));
    }
}
