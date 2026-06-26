package com.mlops.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

// catch-all mapper - handles anything that wasn't caught by a more specific mapper
// prevents raw stack traces from leaking to the client
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        // if it's a JAX-RS WebApplicationException (like 404/405), just use its built-in response
        // we don't want to turn a proper 404 into a 500
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse();
        }

        // log unexpected errors on the server side for debugging
        logger.severe("Unhandled exception: " + e.getClass().getName() + " - " + e.getMessage());

        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "InternalServerError");
        errorBody.put("message", "An unexpected error occurred. Please try again later.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
