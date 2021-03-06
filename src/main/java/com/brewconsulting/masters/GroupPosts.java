package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.GroupPost;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Task;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by lcom16 on 1/12/16.
 */
@Path("groupposts")
@Secured
public class GroupPosts {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(GroupPosts.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces Group Post list
     *
     * @param groupId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{groupId}")
    public Response GrpPosts(@PathParam("groupId") Integer groupId,
                                       @Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(GroupPost
                            .getGroupPost(groupId, ((LoggedInUser) crc
                                    .getProperty("userObject"))))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException" , na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get group Meetings\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception" , e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }


    /**
     * Add group post
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    public Response createGrpPost(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int groupPostId = GroupPost.addGroupPost(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (groupPostId != 0)
                resp = Response.ok("{\"id\":" + groupPostId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Group post")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException" , na);
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Insert Group Post").build();
        } catch (IOException e) {
            logger.error("IOException" , e);
            if (resp == null) {
                resp = Response.serverError().entity(e.getMessage()).build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception" , e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}
