package com.brewconsulting.masters;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Territorie;
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	public Response territories(@PathParam("id") Integer id,@Context ContainerRequestContext crc) {
		Response resp = null;
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	public Response createTerri(InputStream input,@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int territorieId  = Territorie.addTerritorie(node, (LoggedInUser) crc.getProperty("userObject"));			
			resp = Response.ok("{\"id\":"+territorieId+"}").build();

		} 
		catch (IOException e) 
		{
			if (resp == null)
			{
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
	public Response updateTerri(InputStream input,@Context ContainerRequestContext crc) {
		Response resp = null;
		try {
			JsonNode node = mapper.readTree(input);
			int affectedRow = 	Territorie.updateTerritorie(node,  (LoggedInUser) crc.getProperty("userObject"));
			if(affectedRow >0)
				resp = Response.ok().build();
			else
				resp = Response.status(204).build();
		}
		catch (IOException e) 
		{
			if (resp == null)
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
		}catch (Exception e) {
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
	public Response deleteTerri(@PathParam("id") Integer id,@Context ContainerRequestContext crc) {
		Response resp = null;
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}
}
