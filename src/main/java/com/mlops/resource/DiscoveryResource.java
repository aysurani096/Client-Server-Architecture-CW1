package com.mlops.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

// root endpoint - gives clients a map of what's available in the API
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> info = new HashMap<>();
        info.put("apiVersion", "v1");
        info.put("description", "MLOps Pipeline Management API");
        info.put("contact", "admin@mlops-lab.ac.uk");

        // map of available resource collections so clients know where to go
        Map<String, String> resources = new HashMap<>();
        resources.put("workspaces", "/api/v1/workspaces");
        resources.put("models", "/api/v1/models");

        info.put("resources", resources);

        return Response.ok(info).build();
    }
}
