package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Path("divisions")
@Secured
public class Divisions {
	ObjectMapper mapper = new ObjectMapper();

	static final Logger logger = Logger.getLogger(Divisions.class);
	Properties properties = new Properties();
	InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");
	/***
	 * Produces a list of all divisions
	 *
	 * @param crc
	 * @return
     */
	@GET
	@Produces("application/json")
	@Secured
	public Response divisions(@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			properties.load(inp);
			PropertyConfigurator.configure(properties);

			resp = Response.ok(
					mapper.writeValueAsString(Division
							.getAllDivisions((LoggedInUser) crc
									.getProperty("userObject")))).build();

		} catch (NotAuthorizedException na) {
			logger.error("NotAuthorizedException" , na);
			resp = Response.status(Response.Status.FORBIDDEN)
					.entity("{\"Message\":" + "\"You are not authorized to get Divisions\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		catch (Exception e) {
			logger.error("Exception " , e);
			resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		}

		return resp;
	}

	/***
	 * get a particular division
	 * 
	 * @param id
	 * @param crc
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response divisions(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			properties.load(inp);
			PropertyConfigurator.configure(properties);

			Division div = Division.getDivisionById(id,
					(LoggedInUser) crc.getProperty("userObject"));
			if (div == null) {
				resp = Response
						.noContent()
						.entity(new NoDataFound("This division does not exist")
								.getJsonString()).build();
			} else {
				resp = Response.ok(mapper.writeValueAsString(div)).build();
			}

		} catch (NotAuthorizedException na) {
			logger.error("NotAuthorizedException",na);
			resp = Response.status(Response.Status.FORBIDDEN)
					.entity("{\"Message\":" + "\"You are not authorized to get Division\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		} catch (Exception e) {
			logger.error("Exception " ,e);
			resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * add a new division
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */
	@POST
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response createDiv(InputStream input,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			properties.load(inp);
			PropertyConfigurator.configure(properties);

			JsonNode node = mapper.readTree(input);
			int divisionId = Division.addDivision(node,
					(LoggedInUser) crc.getProperty("userObject"));
			resp = Response.ok("{\"id\":" + divisionId + "}").build();
		} catch (NotAuthorizedException na) {
			logger.error("NotAuthorizedException",na);
			resp = Response.status(Response.Status.FORBIDDEN)
					.entity("{\"Message\":" + "\"You are not authorized to create Division\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		catch (SQLException s)
		{
			logger.error("SQLException",s);
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"Message\":" + "\"" + s.getMessage()  +"\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		catch (IOException e) {
			logger.error("IOException",e);
			if (resp == null) {
				resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("Exception " , e);
			resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * updates a division. the id of the division is passed in input json sample
	 * data {"divId":1,"divName":"Some name","desc":"some description"}
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */

	@PUT
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response updateDiv(InputStream input,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			properties.load(inp);
			PropertyConfigurator.configure(properties);

			JsonNode node = mapper.readTree(input);
			Division.updateDivision(node,
					(LoggedInUser) crc.getProperty("userObject"));
			resp = Response.ok().build();
		}catch (NotAuthorizedException na) {
			logger.error("NotAuthorizedException",na);
			resp = Response.status(Response.Status.FORBIDDEN)
					.entity("{\"Message\":" + "\"You are not authorized to update Division\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		catch (IOException e) {
			logger.error("IOException" ,e);
			if (resp == null)
				resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception" ,e);
			// TODO Auto-generated catch block
			resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		}
		return resp;
	}

	/***
	 * delete a division.
	 * 
	 * @param id
	 * @param crc
	 * @return
	 */
	@DELETE
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response deleteDiv(@PathParam("id") Integer id,
			@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			properties.load(inp);
			PropertyConfigurator.configure(properties);
			// affectedRow given how many rows deleted from database.
			int affectedRow = Division.deleteDivision(id,
					(LoggedInUser) crc.getProperty("userObject"));
			if (affectedRow > 0)
				resp = Response.ok().build();
			else
				// If no rows affected in database. It gives server status
				// 204(NO_CONTENT).
				resp = Response.status(204).entity("{\"Message\":\" + \"\"Division is not deleted.\"}").build();

		}catch (NotAuthorizedException na) {
			logger.error("NotAuthorizedException",na);
			resp = Response.status(Response.Status.FORBIDDEN)
					.entity("{\"Message\":" + "\"You are not authorized to Delete Division\"}")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		catch (PSQLException ex) {
			logger.error("PSQLException " , ex);
			resp = Response
					.status(Response.Status.CONFLICT)
					.entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
					.type(MediaType.APPLICATION_JSON).build();
			ex.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception" ,e);
			if (resp == null)
				resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
			e.printStackTrace();
		}
		return resp;
	}
}