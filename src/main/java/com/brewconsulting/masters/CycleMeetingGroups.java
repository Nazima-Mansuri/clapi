package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.CycleMeetingGroup;
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

@Path("cyclemeetinggroup")
@Secured
public class CycleMeetingGroups {

    ObjectMapper mapper = new ObjectMapper();


    /***
     * Produces list of group meetings with its childs.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response divisionBymeetings(@PathParam("id") Integer id,
                                          @Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingGroup
                            .getMeetingByDivisionId(id, ((LoggedInUser) crc
                                    .getProperty("userObject"))))).build();
        } catch (NotAuthorizedException na) {
            resp = Response
                    .status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get Cycle meeting group ")
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Produce particular Group meeting.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("meetingbyid/{id}")
    public Response groupMeetingById(@PathParam("id") Integer id,
                                       @Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingGroup
                            .getGroupById(id, ((LoggedInUser) crc
                                    .getProperty("userObject"))))).build();
        } catch (NotAuthorizedException na) {
            resp = Response
                    .status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Particular meeting")
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /**
     *  add new Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createMeeting(InputStream input,
                                @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int parentMeetingId = CycleMeetingGroup.addCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (parentMeetingId != 0)
                resp = Response.ok("{\"id\":" + parentMeetingId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Group Meeting")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Group Meeting").build();
        } catch (IOException e) {
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /**
     * Update a Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateMeeting(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            CycleMeetingGroup.updateCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        }catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update group meeting")
                    .build();
        }
        catch (IOException e) {
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /**
     * Delete Meeting
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteMeeting(@PathParam("id") Integer id,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = CycleMeetingGroup.deleteCycleMeeting(id,
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
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();

        }
        return resp;
    }
}
