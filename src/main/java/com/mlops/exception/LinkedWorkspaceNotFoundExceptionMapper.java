package com.mlops.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

// maps LinkedWorkspaceNotFoundException to HTTP 422 Unprocessable Entity
// note: Response.Status doesn't have UNPROCESSABLE_ENTITY in JAX-RS 2.1, so using int directly
@Provider
public class LinkedWorkspaceNotFoundExceptionMapper implements ExceptionMapper<LinkedWorkspaceNotFoundException> {

    @Override
    public Response toResponse(LinkedWorkspaceNotFoundException e) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "LinkedWorkspaceNotFound");
        errorBody.put("message", e.getMessage());

        return Response.status(422)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
