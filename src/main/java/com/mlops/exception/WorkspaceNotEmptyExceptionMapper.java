package com.mlops.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

// maps WorkspaceNotEmptyException to HTTP 409 Conflict
@Provider
public class WorkspaceNotEmptyExceptionMapper implements ExceptionMapper<WorkspaceNotEmptyException> {

    @Override
    public Response toResponse(WorkspaceNotEmptyException e) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "WorkspaceNotEmpty");
        errorBody.put("message", e.getMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
