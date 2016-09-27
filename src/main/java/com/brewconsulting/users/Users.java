package com.brewconsulting.users;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.User;
import com.brewconsulting.DB.UserProfile;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.RequiredDataMissing;
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

	@POST
	@Produces("application/json")
	@Secured
	public Response user(InputStream input, @Context ContainerRequestContext crc) {

		ObjectMapper mapper = new ObjectMapper();
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int userid = UserProfile.createUser(node, (JsonNode) crc.getProperty("user"));
			resp = Response.ok("{\"id\":"+userid+"}").build();
		} catch (IOException e) {
			if (resp == null)
				resp = Response.serverError().header("content-type", MediaType.TEXT_PLAIN).entity(e.getStackTrace())
						.build();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if (resp == null)
				resp = Response.serverError().header("content-type", MediaType.TEXT_PLAIN).entity(e.getStackTrace())
						.build();
			e.printStackTrace();
		} catch (SQLException e) {
			if (resp == null)
				resp = Response.serverError().header("content-type", MediaType.TEXT_PLAIN).entity(e.getStackTrace())
						.build();
			e.printStackTrace();
		} catch (RequiredDataMissing e) {
			resp = Response.serverError().entity(e.getJsonString()).build();
			e.printStackTrace();
		}
		return resp;

	}
	
	/***
	 * Produces a List of Users which are not associate to any Territory.
	 * 
	 * @param crc
	 * @return
	 */

	@GET
	@Produces("application/json")
	@Secured
	@Path("deassociateuser")
	public Response daassUser(@Context ContainerRequestContext crc) {
		Response resp = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			resp = Response.ok(mapper.writeValueAsString(UserProfile.getDeassociateUser((LoggedInUser)crc.getProperty("userObject"))) ).build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}
	
	/***
	 * Produces a List of All Users.
	 * 
	 * @param crc
	 * @return
	 */

	@GET
	@Produces("application/json")
	@Secured
	@Path("allUser")
	public Response getAllUsers(@Context ContainerRequestContext crc) {
		Response resp = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			resp = Response.ok(mapper.writeValueAsString(UserProfile.getAllUsers((LoggedInUser)crc.getProperty("userObject"))) ).build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}
}