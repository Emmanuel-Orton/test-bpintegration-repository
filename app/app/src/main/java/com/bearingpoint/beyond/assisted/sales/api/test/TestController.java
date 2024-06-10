package com.bearingpoint.beyond.test-bpintegration.api.test;

import com.bearingpoint.beyond.test-bpintegration.api.test.model.TestModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/test")
@Api(tags = "TelusTasksV1Api")
public class TestController {

    public static final String HEALTHY = "healthy";

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    @ApiOperation(value = "Simple API for testing."
            , notes = "Simple API for testing.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public String test(@RequestHeader(name = "env", required = false) String env) {
        return HEALTHY;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    @ApiOperation(value = "Simple API for testing."
            , notes = "Simple API for testing.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public String testPost(@RequestHeader(name = "env", required = false) String env, @RequestBody TestModel testModel) {

        return HEALTHY;
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping
    @ApiOperation(value = "Simple API for testing."
            , notes = "Simple API for testing.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public String testPatch(@RequestHeader(name = "env", required = false) String env, @RequestBody TestModel testModel) {
        return HEALTHY;
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    @ApiOperation(value = "Simple API for testing."
            , notes = "Simple API for testing.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public String testPut(@RequestHeader(name = "env", required = false) String env, @RequestBody TestModel testModel) {
        return HEALTHY;
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping
    @ApiOperation(value = "Simple API for testing."
            , notes = "Simple API for testing.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public String testDelete(@RequestHeader(name = "env", required = false) String env) {
        return HEALTHY;
    }
}
