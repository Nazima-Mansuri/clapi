package com.brewconsulting.users;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.User;
import com.brewconsulting.DB.UserProfile;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("users")
@Secured
public class Users {

	/***
	 * Get the details of the user. User profile, and roles he is in.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Path("{id}")
	@Secured
	public Response user(@PathParam("id") Integer id, @Context ContainerRequestContext crc) {
		Response resp = null;
		ObjectMapper mapper = new ObjectMapper();

		try {
			resp = Response.ok(mapper.writeValueAsString(User.getProfile((JsonNode) crc.getProperty("user"), id)))
					.build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED).header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to view other's profile").build();
		} catch (Exception e) {
			if (resp == null)
				resp = Response.serverError().header("content-type", MediaType.TEXT_PLAIN).entity(e.getStackTrace())
						.build();
			e.printStackTrace();
		}

		return resp;

	}
}