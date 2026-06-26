package com.mlops.resource;

import com.mlops.exception.LinkedWorkspaceNotFoundException;
import com.mlops.model.MachineLearningModel;
import com.mlops.model.MLWorkspace;
import com.mlops.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // get all models, with optional filtering by status
    @GET
    public Response getModels(@QueryParam("status") String status) {
        // if no filter is given just return everything
        if (status == null || status.isEmpty()) {
            return Response.ok(new ArrayList<>(store.getModels().values())).build();
        }

        // filter by status using a simple for-loop
        List<MachineLearningModel> filtered = new ArrayList<>();
        for (MachineLearningModel model : store.getModels().values()) {
            if (model.getStatus().name().equalsIgnoreCase(status)) {
                filtered.add(model);
            }
        }

        return Response.ok(filtered).build();
    }

    // register a new model - must link to an existing workspace
    @POST
    public Response createModel(MachineLearningModel model) {
        // check the workspace actually exists before we do anything else
        String workspaceId = model.getWorkspaceId();
        MLWorkspace workspace = store.getWorkspaces().get(workspaceId);

        if (workspace == null) {
            throw new LinkedWorkspaceNotFoundException(
                    "workspace '" + workspaceId + "' does not exist. "
                    + "Create the workspace first before adding models to it."
            );
        }

        // server generates the ID - don't trust client to send a unique one
        String newId = "MOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        model.setId(newId);

        store.getModels().put(newId, model);

        // add this model's ID to the workspace so we can track what's in it
        workspace.getModelIds().add(newId);

        // initialise an empty metrics list for this model
        store.initMetricsForModel(newId);

        return Response.created(
                uriInfo.getAbsolutePathBuilder().path(newId).build()
        ).entity(model).build();
    }

    // sub-resource locator for metrics - no HTTP method annotation here on purpose
    // JAX-RS sees the @Path but no verb, so it knows to delegate to MetricSubResource
    @Path("/{modelId}/metrics")
    public MetricSubResource getMetricSubResource(@PathParam("modelId") String modelId) {
        return new MetricSubResource(modelId);
    }
}
