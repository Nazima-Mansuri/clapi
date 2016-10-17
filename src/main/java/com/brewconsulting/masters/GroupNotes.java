package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Note;
import com.brewconsulting.DB.masters.Task;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Insert Group Note").build();
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
}
