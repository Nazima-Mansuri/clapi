package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Note;
import com.brewconsulting.DB.masters.Task;
import com.brewconsulting.DB.masters.UserViews;
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
 * Created by lcom53 on 17/10/16.
 */

@Path("groupnotes")
@Secured
public class GroupNotes {
    ObjectMapper mapper = new ObjectMapper();

    /***
     * Produces list of notes for Specific Group
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Secured
    @Produces("application/json")
    @Path("{groupid}")
    public Response getGrpNotes(@PathParam("groupid") int id ,@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resp = Response.ok(
                    mapper.writerWithView(UserViews.groupNoteView.class).writeValueAsString(Note
                            .getAllGroupNotes(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to get group notes\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * get a particular Group Note.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("notbyid/{id}")
    public Response getGrpNotes(@PathParam("id") Integer id,
                                @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            Note note = Note.getGroupNoteById(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (note == null) {
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("This Group Note does not exist")
                                .getJsonString()).build();
            } else {
                resp = Response.ok(mapper.writerWithView(UserViews.groupNoteView.class).writeValueAsString(note)).build();
            }

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to get particular group note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }


    /***
     * Add Group Note
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")

    public Response createGrpNote(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int groupNoteId = Note.addGroupNote(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (groupNoteId != 0)
                resp = Response.ok("{\"id\":" + groupNoteId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Group Note")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to add group note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
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

    /***
     * Update Group Note
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")
    public Response updateGrpNote(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            Note.updateGroupNote(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to update group note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Delete Group Note
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Secured
    @Produces("application/json")
    @Path("{id}")
    public Response deleteGrpNote(@PathParam("id") Integer id,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = Note.deleteGroupNote(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to delete group note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
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
