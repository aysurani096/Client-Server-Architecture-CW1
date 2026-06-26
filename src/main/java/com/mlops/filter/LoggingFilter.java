package com.mlops.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

// single class that handles both request and response logging
// runs before every request and after every response
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    // fires before the resource method handles the request
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.info("[REQUEST]  " + requestContext.getMethod()
                + " " + requestContext.getUriInfo().getRequestUri());
    }

    // fires after the resource method has finished and a response is ready to send
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        logger.info("[RESPONSE] " + requestContext.getMethod()
                + " " + requestContext.getUriInfo().getRequestUri()
                + " -> " + responseContext.getStatus());
    }
}
