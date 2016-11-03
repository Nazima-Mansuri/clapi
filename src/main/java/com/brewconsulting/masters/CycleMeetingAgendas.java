package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeetingAgenda;
import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.GroupAgenda;
import com.brewconsulting.DB.masters.LoggedInUser;
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
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Path("cycleMeetingAgendas")
@Secured
public class CycleMeetingAgendas {


    ObjectMapper mapper = new ObjectMapper();


    /**
     * get all cycle meeting agendas
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    public Response cycleMeetingAgenda(@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingAgenda
                            .getAllCycleMeetingAgenda((LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get cycle meeting agenda").build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();

        }

        return resp;
    }


    /**
     * Add cycle meeting agenda.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createCycleMeetingAgenda(InputStream input,
                                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int id = CycleMeetingAgenda.addCycleMeetingAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + id + "}").build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create cycle meeting agenda")
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

    /**
     *  Clone cyclemeeting agenda from group agenda
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("/clone")
    public Response cloneAgenda(InputStream input , @Context ContainerRequestContext crc)
    {
        Response resp = null;
        try
        {
            JsonNode node = mapper.readTree(input);
            List<Integer> list = new ArrayList<>();
            list = CycleMeetingAgenda.cloneCycleMeetingAgenda(node,(LoggedInUser) crc.getProperty("userObject"));
            for (int i = 0;i<list.size();i++)
            {
                resp = Response.ok("{\"id\":" + list+ "}").build();
            }
        }
        catch (Exception e)
        {
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        }
        return resp;
    }
    /**
     * Update cycle meeting agenda.
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateCycleMeetingAgenda(InputStream input,
                                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            CycleMeetingAgenda.updateCycleMeetingAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update cycle meeting agenda")
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

    /**
     * Delete cycle meeting agenda.
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteCycleMeetingAgenda(@PathParam("id") Integer id,
                                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = CycleMeetingAgenda.deleteCycleMeetingAgenda(id,
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
                    .entity("You are not authorized to delete cycle meeting agenda")
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
            e.printStackTrace();        }
        return resp;
    }

    /**
     * get agenda by date
     *
     * @param cycleMeetingId
     * @param date
     * @param crc
     * @return
     */
    @GET
    @Secured
    @Produces("application/json")
    @Path("/date/{cycleMeetingId}/{date}")
    public Response getAgendaByDate(@PathParam("cycleMeetingId") Integer cycleMeetingId, @PathParam("date") Date date,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingAgenda
                            .getAgendaByDate(cycleMeetingId, date, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get cycle meeting agenda")
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
