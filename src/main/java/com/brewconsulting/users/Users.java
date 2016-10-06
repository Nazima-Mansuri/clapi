package com.brewconsulting.users;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.masters.Product;
import org.postgresql.util.PSQLException;

import com.brewconsulting.DB.masters.User;
import com.brewconsulting.DB.masters.UserViews;
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
	public Response user(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		ObjectMapper mapper = new ObjectMapper();

		try {
			resp = Response.ok(
					mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User.getProfile(
							(LoggedInUser)crc.getProperty("userObject"), id))).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to view other's profile")
					.build();
		} catch (Exception e) {
			if (resp == null)
				resp = Response.serverError()
						.header("content-type", MediaType.TEXT_PLAIN)
						.entity(e.getStackTrace()).build();
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
			int userid = User.createUser(node,
					(LoggedInUser) crc.getProperty("userObject"));
			resp = Response.ok("{\"id\":" + userid + "}").build();
		 
		} catch (IOException e) {
			if (resp == null)
				resp = Response.serverError()
						.header("content-type", MediaType.TEXT_PLAIN)
						.entity(e.getStackTrace()).build();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if (resp == null)
				resp = Response.serverError()
						.header("content-type", MediaType.TEXT_PLAIN)
						.entity(e.getStackTrace()).build();
			e.printStackTrace();

		} catch (PSQLException e) {

			resp = Response.status(409).entity(e.getMessage())
					.type(MediaType.TEXT_PLAIN).build();
			e.printStackTrace();

		} catch (SQLException e) {
			if (resp == null)
				resp = Response.serverError()
						.header("content-type", MediaType.TEXT_PLAIN)
						.entity(e.getStackTrace()).build();
			e.printStackTrace();

		} catch (RequiredDataMissing e) {
			resp = Response.serverError().entity(e.getJsonString()).build();
			e.printStackTrace();

		} catch (NamingException e) {
			if (resp == null)
				resp = Response.serverError()
						.header("content-type", MediaType.TEXT_PLAIN)
						.entity(e.getStackTrace()).build();
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
			resp = Response.ok(
					mapper.writerWithView(UserViews.deAssociateView.class).writeValueAsString(User
							.getDeassociateUser((LoggedInUser) crc
									.getProperty("userObject")))).build();
		} catch (NotAuthorizedException na) { 
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to get Deassociate User")
					.build();
		}

		catch (Exception e) {
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
			resp = Response.ok(
					mapper.writerWithView(UserViews.bareView.class).writeValueAsString(User
							.getAllUsers((LoggedInUser) crc
									.getProperty("userObject")))).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to get Users").build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * Delete a Product
	 *
	 * @param id
	 * @param crc
	 * @return
	 */
	@DELETE
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response deleteUser(@PathParam("id") Integer id,
							  @Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			System.out.println("In Del");
			int affectedRow = UserProfile.deleteUser(id,
					(LoggedInUser) crc.getProperty("userObject"));
			System.out.println("Method called and affected rows" + affectedRow);
			if (affectedRow > 0)
				resp = Response.ok().build();
			else
				// If no rows affected in database. It gives server status
				// 204(NO_CONTENT).
				resp = Response.status(204).build();

		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to delete User").build();
		} catch (PSQLException ex) {
			resp = Response
					.status(409)
					.entity("This id is already Use in another table as foreign key")
					.type(MediaType.TEXT_PLAIN).build();
			ex.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}

}