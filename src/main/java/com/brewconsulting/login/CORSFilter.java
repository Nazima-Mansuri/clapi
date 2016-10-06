package com.brewconsulting.login;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CORSFilter implements ContainerResponseFilter,ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext arg0, ContainerResponseContext cres) throws IOException {
		cres.getHeaders().add("Access-Control-Allow-Origin", "*");

	}

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        requestContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        requestContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        requestContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        requestContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

    }
}
