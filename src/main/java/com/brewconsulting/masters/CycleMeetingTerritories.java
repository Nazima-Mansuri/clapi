package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeeting;
import com.brewconsulting.DB.masters.CycleMeetingTerritory;
import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lcom53 on 3/11/16.
 */

@Path("cyclemeetingterritories")
@Secured
public class CycleMeetingTerritories {

    ObjectMapper mapper = new ObjectMapper();

    /***
     *  Produces Cyclemeeting Territories
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response meetingsTerr(@PathParam("id") int id , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllCycleMeetingTerr(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cycle meeting Territories\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Add Cyclemeeting Territory
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createMeetingTerr(InputStream input,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int count = CycleMeetingTerritory.addCycleMeetingTerr(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"Count\":" + count + "}").build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Cyclemeeting Territory\"}")
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
}
