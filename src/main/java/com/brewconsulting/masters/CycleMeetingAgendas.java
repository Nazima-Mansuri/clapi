package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeetingAgenda;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


@Path("cycleMeetingAgendas")
@Secured
public class CycleMeetingAgendas {


    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(CycleMeetingAgendas.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");
    /**
     * Produces all cycle meeting agendas.
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingAgenda
                            .getAllCycleMeetingAgenda((LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeeting Agenda\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception " ,e);
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int id = CycleMeetingAgenda.addCycleMeetingAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + id + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Cyclemeeting Agenda\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException " ,e);

            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception " ,e);
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            List<Integer> list = new ArrayList<>();
            list = CycleMeetingAgenda.cloneCycleMeetingAgenda(node,(LoggedInUser) crc.getProperty("userObject"));
            for (int i = 0;i<list.size();i++)
            {
                resp = Response.ok("{\"id\":" + list+ "}").build();
            }
        }
        catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to clone Cyclemeeting Agenda\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (BadRequestException b) {
            logger.error("BadRequestException " ,b);
            resp = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"Message\":" + "\"Agenda is already exist with same time.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (Exception e)
        {
            logger.error("Exception " ,e);
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            CycleMeetingAgenda.updateCycleMeetingAgenda(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Cyclemeeting Agenda\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (BadRequestException b) {
            logger.error("BadRequestException " ,b);
            resp = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"Message\":" + "\"Agenda is already exist with same time.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            logger.error("IOException " ,e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception " ,e);
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

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
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to delete Cyclemeeting Agenda\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
            logger.error("PSQLException " ,ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception " ,e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();        }
        return resp;
    }

    /**
     * get cycle meeting agenda by specific date.
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
    public Response getAgendaByDate(@PathParam("cycleMeetingId") Integer cycleMeetingId, @PathParam("date") String date,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {

            properties.load(inp);
            PropertyConfigurator.configure(properties);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date parseDate = sdf.parse(date);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingAgenda
                            .getAgendaByDate(cycleMeetingId, parseDate, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeeting Agenda by specific date.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException " ,e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception " ,e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

}
