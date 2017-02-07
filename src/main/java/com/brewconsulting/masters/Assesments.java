package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Assesment;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.QuestionCollection;
import com.brewconsulting.DB.masters.UserViews;
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
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by lcom62_one on 2/2/2017.
 */

@Path("assesments")
@Secured
public class Assesments {

    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Assesments.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces All Assesment details.
     *
     * @param isGroup
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{divId}")
    public Response getAllAssesments(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.assesmentView.class).writeValueAsString(Assesment
                            .getAllAssesments(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Assesments\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     *  Produces Assesment Setting details.
     *
     * @param testId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("setting/{testId}")
    public Response getAssesmentSettings(@PathParam("testId") int testId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.settingView.class).writeValueAsString(Assesment
                            .getSettingDetails(testId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Assesments\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     *  Add Assesments
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createAssesment(InputStream input,
                                         @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int assesmentId = Assesment.addOnTheGoAssesment(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + assesmentId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create On The Gp Assesment\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (SQLException s)
        {
            logger.error("SQLException",s);
            resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"Message\":" + "\"" + s.getMessage()  +"\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            logger.error("IOException",e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception " , e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Update Assesment
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateAssesment(InputStream input,
                                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRow = Assesment.updateAssesment(node,
                    (LoggedInUser) crc.getProperty("userObject"));

            resp = Response.ok("{\"affectedRow\":" + affectedRow + "}").build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Assesment\"}")
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

    /***
     *  Update Assesment Setting
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("setting")
    public Response updateAssesmentSettings(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRow = Assesment.updateSettings(node,
                    (LoggedInUser) crc.getProperty("userObject"));

            resp = Response.ok("{\"affectedRow\":" + affectedRow + "}").build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException " ,na);

            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Assesment Settings\"}")
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

    /***
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteAssesment(@PathParam("id") Integer id,
                                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            // affectedRow given how many rows deleted from database.
            int affectedRow = Assesment.deleteAssesment(id,
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
                    .entity("{\"Message\":" + "\"You are not authorized to delete Assesment\"}")
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
}
