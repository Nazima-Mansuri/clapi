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
 * Created by lcom16 on 17/10/16.
 */
@Path("cyclemeetingnotes")
@Secured
public class ChildNotes {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Produces all Cycle Meeting Notes
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Secured
    @Path("{cyclemeetingid}")
    public Response getAllChildNotes(@PathParam("cyclemeetingid") int id, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resp = Response.ok(
                    mapper.writerWithView(UserViews.groupNoteView.class).writeValueAsString(Note
                            .getAllChildNote(id, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeeting Notes\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();
        }
        return resp;
    }

    /**
     * Produces Particulat Cycle Meeting Note.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Secured
    @Path("/notesbyid/{id}")
    public Response getChildNoteById(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resp = Response.ok(
                    mapper.writerWithView(UserViews.groupNoteView.class).writeValueAsString(Note
                            .getChildNoteById(id, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeeting Note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();
        }
        return resp;
    }

    /**
     * Add Cycle Meeting Note
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")

    public Response createChildNote(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int childNoteId = Note.addChildNote(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (childNoteId != 0)
                resp = Response.ok("{\"id\":" + childNoteId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Cycle Meeting Note")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Cyclemeeting Note\"}")
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
     * Update child note
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")

    public Response updateChildNote(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            Note.updateChildNote(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        }catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Cyclemeeting Note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }  catch (IOException e) {
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
     * Delete child note
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Secured
    @Produces("application/json")
    @Path("{id}")
    public Response deleteChildNote(@PathParam("id") Integer id,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = Note.deleteChildNote(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to delete Cyclemeeting Note\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }  catch (PSQLException ex) {
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
