package com.brewconsulting;

import javax.annotation.Resource;
import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")

public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)

	public String getIt(@Context ServletContext context) {

		
		if (context != null)
			return "got it bro"+context.getInitParameter("salt");
		else return "context missing";
	}
}
