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
	
	/**
	 *TODO: implement these steps
	 * 1. check if the token supplied is ACCESS TOKEN. I have added this field to token
	 * 2. if not access token 400 BAD REQUEST - the client should send only access token for API access
	 * 3. Scenarios: LOGOUT, USER DEACTIVATED, USER DIV MAPPING CHANGE, USER NAME CHANGE, USER ROLE CHANGE
	 * 4. if LOGOUT: the client should delete both token so they are never used again. On server put both in memcached 
	 * with a life of (configurable) 1 month. thus for the next one month the tokens are not usable. This also means
	 * that both these tokens for public and work domains should never be more than this value. If any token received that 
	 * is logged out then 401 unauthorised. The client will need to show login page.
	 * 5. USER DEACTIVATED - same as logout. Put both tokens in deactivate key value pairs in memcached
	 * 6. DIV MAPPING CHANGED: if the access token is in this key-value pair (it will be put from update user 
	 * function in user class -check existing divs from access token and if the updated divs differ put it here. so 
	 * return 401 to client. the client will then send refresh token where you will goto DB and create access token
	 * with correct values.
	 * 7. USER NAME CHANGE and ROLE CHANGE - same as DIV mapping change. 
	 *  
	 */

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
				String tokenType = (String) clms.getBody().get("tokenType");
				
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
