package com.mlops.resource;

import com.mlops.exception.ModelDeprecatedException;
import com.mlops.model.EvaluationMetric;
import com.mlops.model.MachineLearningModel;
import com.mlops.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// no @Path annotation at class level - this class is only reached via the
// sub-resource locator in ModelResource, not as a root resource
public class MetricSubResource {

    private final String modelId;
    private final DataStore store = DataStore.getInstance();

    // modelId gets injected from the locator method in ModelResource
    public MetricSubResource(String modelId) {
        this.modelId = modelId;
    }

    // get all evaluation metrics for this model
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics() {
        if (!store.getModels().containsKey(modelId)) {
            throw new NotFoundException("model not found: " + modelId);
        }

        List<EvaluationMetric> metricList = store.getMetrics()
                .getOrDefault(modelId, new ArrayList<>());

        return Response.ok(metricList).build();
    }

    // add a new evaluation metric for this model
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMetric(EvaluationMetric metric, @Context UriInfo uriInfo) {
        MachineLearningModel model = store.getModels().get(modelId);

        if (model == null) {
            throw new NotFoundException("model not found: " + modelId);
        }

        // deprecated models don't get new metrics - they're no longer being monitored
        if (model.getStatus() == MachineLearningModel.ModelStatus.DEPRECATED) {
            throw new ModelDeprecatedException(
                    "model '" + modelId + "' is DEPRECATED and cannot receive new evaluation metrics."
            );
        }

        // server sets the id and timestamp - don't rely on client for these
        metric.setId(UUID.randomUUID().toString());
        metric.setTimestamp(Instant.now().toString());

        store.getMetrics().get(modelId).add(metric);

        // update the parent model's latestAccuracy to keep things consistent
        model.setLatestAccuracy(metric.getAccuracyScore());

        return Response.created(
                uriInfo.getAbsolutePathBuilder().path(metric.getId()).build()
        ).entity(metric).build();
    }
}
