package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.*;
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


@Path("groupAgendas")
@Secured
public class GroupAgendas {


    ObjectMapper mapper = new ObjectMapper();

    /**
     * Method Add group agenda.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createGroupAgenda(InputStream input,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int id = GroupAgenda.addGroupAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + id + "}").build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create group agenda")
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


    /**
     * Method update group agenda.
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateGroupAgenda(InputStream input,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            GroupAgenda.updateGroupAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update group agenda")
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

    /**
     * Delete group agenda
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteGroupAgenda(@PathParam("id") Integer id,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = GroupAgenda.deleteGroupAgenda(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to delete group agenda")
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
     * get group agenda by day.
     *
     * @param groupId
     * @param dayNo
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("/day/{groupId}/{dayNo}")
    public Response getAgendaByDay(@PathParam("groupId") Integer groupId, @PathParam("dayNo") Integer dayNo,
                                   @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(GroupAgenda
                            .getAgendaByDay(groupId, dayNo, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
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


}
