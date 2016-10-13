package com.brewconsulting.masters;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.postgresql.util.PSQLException;

import com.brewconsulting.DB.masters.History;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Territory;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("territories")
public class Territories {
	ObjectMapper mapper = new ObjectMapper();

	/***
	 * Produces a list of all territories
	 * 
	 * @param crc
	 * @return
	 */

	@GET
	@Produces("application/json")
	@Secured
	public Response territories(@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			resp = Response.ok(
					mapper.writeValueAsString(Territory
							.getAllTerritories((LoggedInUser) crc
									.getProperty("userObject"),3))).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to get territories")
					.build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * get a particular territorie
	 * 
	 * @param id
	 * @param crc
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response territories(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			Territory terr = Territory.getTerritorieById(id,
					(LoggedInUser) crc.getProperty("userObject"));
			if (terr == null) {
				resp = Response
						.noContent()
						.entity(new NoDataFound("This Territory does not exist")
								.getJsonString()).build();
			} else
				resp = Response.ok(mapper.writeValueAsString(terr)).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to get territory").build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * Produces a list of all territories of Particular division.
	 * 
	 * @param crc
	 * @return
	 */

	@GET
	@Produces("application/json")
	@Secured
	@Path("divisionbyid/{id}")
	public Response divisionByterritories(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;

		try {
			resp = Response.ok(
					mapper.writeValueAsString(Territory
							.getTerritorieByDivisionId(id, ((LoggedInUser) crc
									.getProperty("userObject"))))).build();
		} catch (NotAuthorizedException na) {
			resp = Response
					.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to get divison by territory")
					.build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * add a new territorie
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */
	@POST
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response createTerri(InputStream input,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int territoryId = Territory.addTerritory(node,
					(LoggedInUser) crc.getProperty("userObject"));
			if (territoryId != 0)
				resp = Response.ok("{\"id\":" + territoryId + "}").build();
			else
				resp = Response
						.noContent()
						.entity(new NoDataFound("Unable to Insert Territory")
								.getJsonString()).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to add Territory").build();
		} catch (IOException e) {
			if (resp == null) {
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * updates a territorie. The id of the territorie is passed in input json
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */

	@PUT
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response updateTerri(InputStream input,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int affectedRow = Territory.updateTerritory(node,
					(LoggedInUser) crc.getProperty("userObject"));
			if (affectedRow > 0)
				resp = Response.ok().build();
			else
				resp = Response
						.noContent()
						.entity(new NoDataFound("Unable to update Territory")
								.getJsonString()).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to update Territory")
					.build();
		} catch (IOException e) {
			if (resp == null)
				resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * delete territorie.
	 * 
	 * @param id
	 * @param crc
	 * @return
	 */
	@DELETE
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response deleteTerri(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			int affectedRow = Territory.deleteTerritory(id,
					(LoggedInUser) crc.getProperty("userObject"));
			if (affectedRow > 0)
				resp = Response.ok().build();
			else
				// If no rows affected in database. It gives server status
				// 204(NO_CONTENT).
				resp = Response
						.noContent()
						.entity(new NoDataFound(
								"This Territory Id does not exist")
								.getJsonString()).build();

		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to delete Territory")
					.build();
		} catch (PSQLException ex) {
			resp = Response
					.status(409)
					.entity("This id is already Use in another table as foreign key")
					.type(MediaType.TEXT_PLAIN).build();
			ex.printStackTrace();
		} catch (Exception e) {
			if (resp == null)
				resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}

	/**
	 * delete from userTerritoryMap. Update endDate in userTerritoryMapHistory
	 *
	 * @param input
	 * @param crc
     * @return
     */

	@POST
	@Path("/deassociate")
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response deassociateUser(InputStream input,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int affectedRow = Territory.deassociateUser(node,
					(LoggedInUser) crc.getProperty("userObject"));
			if (affectedRow > 0)
				resp = Response.ok().build();
			else
				// If no rows affected in database. It gives server status
				// 204(NO_CONTENT).
				resp = Response
						.noContent()
						.entity(new NoDataFound(
								"This Territory Id does not exist")
								.getJsonString()).build();
		} catch (NotAuthorizedException na) {
			resp = Response.status(Response.Status.UNAUTHORIZED)
					.header("content-type", MediaType.TEXT_PLAIN)
					.entity("You are not authorized to deassociate User")
					.build();
		} catch (IOException e) {
			if (resp == null) {
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * Produces a History of Territory
	 * 
	 * @param crc
	 * @return
	 */

	@GET
	@Produces("application/json")
	@Secured
	@Path("history/{id}")
	public Response histories(@PathParam("id") Integer id,@Context ContainerRequestContext crc) {
		Response resp = null;

		try {
			resp = Response.ok(
					mapper.writeValueAsString(History
							.getAllHistory(id,(LoggedInUser) crc
									.getProperty("userObject")))).build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		}
		return resp;
	}
}