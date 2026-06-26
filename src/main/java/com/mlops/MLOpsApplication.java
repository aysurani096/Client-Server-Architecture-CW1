package com.mlops;

import com.mlops.exception.GlobalExceptionMapper;
import com.mlops.exception.LinkedWorkspaceNotFoundExceptionMapper;
import com.mlops.exception.ModelDeprecatedExceptionMapper;
import com.mlops.exception.WorkspaceNotEmptyExceptionMapper;
import com.mlops.filter.LoggingFilter;
import com.mlops.resource.DiscoveryResource;
import com.mlops.resource.ModelResource;
import com.mlops.resource.WorkspaceResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

// sets the base path for all API endpoints to /api/v1
@ApplicationPath("/api/v1")
public class MLOpsApplication extends ResourceConfig {

    public MLOpsApplication() {
        // register Jackson so our POJOs get automatically converted to/from JSON
        register(JacksonFeature.class);

        // register all root resource classes
        register(DiscoveryResource.class);
        register(WorkspaceResource.class);
        register(ModelResource.class);

        // MetricSubResource is NOT registered here - it's a sub-resource,
        // Jersey finds it through the locator method in ModelResource

        // exception mappers
        register(WorkspaceNotEmptyExceptionMapper.class);
        register(LinkedWorkspaceNotFoundExceptionMapper.class);
        register(ModelDeprecatedExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        // logging filter
        register(LoggingFilter.class);
    }
}
