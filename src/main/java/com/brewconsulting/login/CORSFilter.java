package com.brewconsulting.login;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CORSFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext arg0, ContainerResponseContext cres) throws IOException {
		cres.getHeaders().add("Access-Control-Allow-Origin", "*");

	}

}
