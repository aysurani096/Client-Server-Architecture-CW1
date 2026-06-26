package com.mlops.resource;

import com.mlops.exception.WorkspaceNotEmptyException;
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

@Path("/workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // return all workspaces in the system
    @GET
    public Response getAllWorkspaces() {
        List<MLWorkspace> workspaceList = new ArrayList<>(store.getWorkspaces().values());
        return Response.ok(workspaceList).build();
    }

    // create a new workspace - server generates the id
    @POST
    public Response createWorkspace(MLWorkspace workspace) {
        String newId = "WS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        workspace.setId(newId);

        // make sure modelIds is initialised (just in case client sent one)
        if (workspace.getModelIds() == null) {
            workspace.setModelIds(new ArrayList<>());
        }

        store.getWorkspaces().put(newId, workspace);

        // 201 Created with a Location header pointing to the new resource
        return Response.created(
                uriInfo.getAbsolutePathBuilder().path(newId).build()
        ).entity(workspace).build();
    }

    // get a specific workspace by ID
    @GET
    @Path("/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = store.getWorkspaces().get(workspaceId);

        if (workspace == null) {
            throw new NotFoundException("workspace not found: " + workspaceId);
        }

        return Response.ok(workspace).build();
    }

    // delete a workspace - but only if it has no models assigned to it
    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = store.getWorkspaces().get(workspaceId);

        if (workspace == null) {
            throw new NotFoundException("workspace not found: " + workspaceId);
        }

        // business rule: can't delete a workspace if it still has models in it
        // this prevents orphaned models floating around with no workspace
        if (!workspace.getModelIds().isEmpty()) {
            throw new WorkspaceNotEmptyException(
                    "cannot delete workspace '" + workspaceId + "' - it still has "
                    + workspace.getModelIds().size() + " model(s) assigned to it. "
                    + "Remove or move the models first."
            );
        }

        store.getWorkspaces().remove(workspaceId);
        // 204 No Content - successful delete, nothing to return
        return Response.noContent().build();
    }
}
