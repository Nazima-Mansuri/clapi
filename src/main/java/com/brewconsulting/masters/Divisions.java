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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.postgresql.util.PSQLException;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("divisions")
@Secured
public class Divisions 
{
	ObjectMapper mapper = new ObjectMapper();
	/***
	 * Produces a list of all divisions
	 * @return
	 */
	
	@GET
	@Produces("application/json")
	@Secured
	public Response divisions( @Context ContainerRequestContext crc){
		Response resp = null;
		
		try {
			resp = Response.ok(mapper.writeValueAsString(Division.getAllDivisions((LoggedInUser)crc.getProperty("userObject"))) ).build();
		} catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		} 
		
		return resp;
	}
	/***
	 * get a particular division
	 * @param id
	 * @param crc
	 * @return
	 */
	@GET
	@Produces("application/json")
	@Secured
	@Path("{id}")
	public Response divisions(@PathParam("id") Integer id,  @Context ContainerRequestContext crc){
		Response resp = null;
		try{
			Division div = Division.getDivisionById(id, (LoggedInUser)crc.getProperty("userObject"));
			if (div == null){
				resp = Response.noContent().entity(new NoDataFound("This division does not exist").getJsonString()).build();
			}
			else resp = Response.ok(mapper.writeValueAsString(div)).build();
		}catch (Exception e) {
			resp = Response.serverError().entity(e.getMessage()).build();
			e.printStackTrace();
		} 
		return resp;
	}
	/*** add a new division
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */
	@POST
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response createDiv(InputStream input,  @Context ContainerRequestContext crc)
	{
		Response resp = null;
		try 
		{
			JsonNode node = mapper.readTree(input);
			int divisionId  = Division.addDivision(node, (LoggedInUser) crc.getProperty("userObject"));			
			resp = Response.ok("{\"id\":"+divisionId+"}").build();
		} 
		catch (IOException e) 
		{
			if (resp == null)
			{
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}
	
	/***
	 * updates a division. the id of the division is passed in input json
	 * sample data {"divId":1,"divName":"Some name","desc":"some description"}
	 * 
	 * @param input
	 * @param crc
	 * @return
	 */
	
	@PUT
	@Produces("application/json")
	@Secured
	@Consumes("application/json")
	public Response updateDiv(InputStream input,  @Context ContainerRequestContext crc){
		Response resp = null;
		try 
		{
			JsonNode node = mapper.readTree(input);
			Division.updateDivision(node, (LoggedInUser) crc.getProperty("userObject"));			
			resp = Response.ok().build();
		} 
		catch (IOException e) 
		{
			if (resp == null)
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
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
	public Response deleteDiv(@PathParam("id") Integer id,  @Context ContainerRequestContext crc){
		Response resp = null;
		try 
		{
			// affectedRow given how many rows deleted from database.
			int affectedRow =Division.deleteDivision(id, (LoggedInUser) crc.getProperty("userObject"));
			if(affectedRow > 0)
				resp = Response.ok().build();
			else
				// If no rows affected in database. It gives server status 204(NO_CONTENT).
				resp = Response.status(204).build();
				
		}
		catch(PSQLException ex)
		{
			resp = Response.status(409).entity("This id is already Use in another table as foreign key").type(MediaType.TEXT_PLAIN).build();
			ex.printStackTrace();
		}
		catch (Exception e) 
		{
			if (resp == null)
				resp = Response.serverError().entity(e.getMessage()).build();
				e.printStackTrace();
				
		}
		return resp;
	}
}
