package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeeting;
import com.brewconsulting.DB.masters.CycleMeetingGroup;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lcom53 on 7/10/16.
 */

@Path("cyclemeeting")
@Secured
public class CycleMeetings {

    ObjectMapper mapper = new ObjectMapper();

    /***
     * Produces a list of all Meetings.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response meetings(@PathParam("id") int id , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeeting
                            .getAllSubMeetings(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get division").build();
        }

        catch (Exception e) {

            resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();

        }
        return resp;
    }

    /***
     * Add Sub Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createSubMeeting(InputStream input,
                                @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int MeetingId = CycleMeeting.addSubCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (MeetingId != 0)
                resp = Response.ok("{\"id\":" + MeetingId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Parent Meeting")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Parent Meeting").build();
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


    /**
     * Update Sub Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateSubMeeting(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            CycleMeeting.updateSubCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        }catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update division")
                    .build();
        }
        catch (IOException e) {
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
     * Delete Sub Meeting
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteSubMeeting(@PathParam("id") Integer id,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = CycleMeeting.deleteSubCycleMeeting(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to delete Meeting")
                    .build();
        }
        catch (PSQLException ex) {
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
}
