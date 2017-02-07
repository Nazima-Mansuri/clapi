package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.Feed;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Pill;
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
 * Created by lcom62_one on 1/13/2017.
 */

@Path("feeds")
@Secured
public class Feeds {

    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Feeds.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces all feeds.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{divId}")
    public Response getAllFeeds(@PathParam("divId") int divId,@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Feed
                            .getAllFeeds(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Feeds \"}")
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
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createFeeds(InputStream input,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int feedId = Feed.addFeeds(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + feedId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Feed\"}")
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
     *
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("pills")
    public Response updateFeed(InputStream input,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = Feed.updatePillsInFeed(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Feeds.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            logger.error("IOException" ,e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception" ,e);
            // TODO Auto-generated catch block
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
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
    public Response deleteFeed(@PathParam("id") Integer id,
                               @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);
            // affectedRow given how many rows deleted from database.
            int affectedRow = Feed.deleteFeeds(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).entity("{\"Message\":\" + \"\"Feed is not deleted.\"}").build();

        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Delete Feed.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (PSQLException ex) {
            logger.error("PSQLException " , ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception" ,e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("removepill")
    public Response removePillFromFeeds(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = Feed.removePillFromFeed(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Remove Pill from feed\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ", e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}
