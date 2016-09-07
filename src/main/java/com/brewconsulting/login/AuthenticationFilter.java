package com.brewconsulting.login;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.elasticsearch.search.rescore.RescoreBuilder;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
	private String salt = null;
	private ServletContext servletContext = null;

	public AuthenticationFilter(@Context ServletContext scon) {
		super();
		this.salt = scon.getInitParameter("salt");
		// System.out.println("SALT VALUE: " + salt);
		this.servletContext = scon;
		servletContext.log("her eis the LOG ENTRY");
	}

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		String authHeader = context.getHeaderString("Authorization");
		// if the auth header is not present reject the request.
		if (authHeader == null) {
			context.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		} else {
			try {
				Jws<Claims> clms = Jwts.parser().setSigningKey(salt).parseClaimsJws(authHeader);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree((String) clms.getBody().get("user"));
				context.setProperty("user", validate(node));
				context.setProperty("userObject", mapper.treeToValue(node, LoggedInUser.class));
			} catch (Exception ex) {
				context.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build());
				servletContext.log("Invalid token", ex);
			}
		}

	}

	private JsonNode validate(JsonNode node) throws Exception {

		if (node.get("id") == null || node.get("id").asInt() < 1)
			throw new Exception("incorrect id");

		if (node.get("clientId") == null || node.get("clientId").asInt() < 1)
			throw new Exception("incorrect client id");

		if (node.get("schemaName") == null)
			throw new Exception("schema not present");

		if (node.get("roles") == null)
			throw new Exception("roles not present");

		if (!node.get("roles").elements().hasNext())
			throw new Exception("User not assigned to any rode");
		return node;
	}

}
