package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Content;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.SettingContent;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
 * Created by lcom53 on 18/10/16.
 */

@Path("groupcontents")
@Secured
public class GroupContents {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(GroupContents.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     * Produces List of Group Contents of specific meeting and division
     *
     * @param meetingId
     * @param divId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{meetingId}/{divId}")
    public Response groupContents(@PathParam("meetingId") int meetingId, @PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getAllGroupContents(meetingId, divId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get group meeting contents \"}")
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
     * Produces List of Group Contents By Specific Agenda
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{agendaId}")
    public Response groupContents(@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getGroupContentByAgenda(agendaId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get group content\"}")
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
     *  Produces list of Mixed group agenda type content.
     *
     * @param agendaId
     * @param contentId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("mixedgroupcontents/{agendaId}/{contentId}")
    public Response groupMixedContents(@PathParam("agendaId") int agendaId, @PathParam("contentId") int contentId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getMixedGroupContents(agendaId, contentId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get group meeting contents \"}")
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
     * Add Group content
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param agendaId
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createGroupContent(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("contentName") String contentName,
            @FormDataParam("contentDesc") String contentDesc,
            @FormDataParam("contentType") String contentType,
            @FormDataParam("divid") int divId,
            @FormDataParam("agendaId") int agendaId,
            @FormDataParam("itemId") int itemId,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (fileFormDataContentDisposition != null) {
                if (fileFormDataContentDisposition.getFileName() != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store content in AWS bucket.
                    uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = "";
                }
            } else {
                uploadFilePath = "";
            }

            int contentId = Content.addGroupContent(contentName, contentDesc, contentType,
                    divId, uploadFilePath, agendaId,itemId, (LoggedInUser) crc.getProperty("userObject"));

            if (contentId != 0)
                resp = Response.ok("{\"id\":" + contentId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Product")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add group meeting content\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ", e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Add Existing Content in Group Content
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createExistGrpContent(InputStream input,
                                          @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = Content.addExistingGroupContent(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Content from Existing content \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException s) {
            logger.error("SQLException ", s);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"" + s.getMessage() + "\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ", e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Delete group Content
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteGrpContent(@PathParam("id") Integer id,
                                     @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            // affectedRow given how many rows deleted from database.
            int affectedRow = Content.deleteGroupContent(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Delete Group Content\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
            logger.error("PSQLException ", ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ", e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Update group sequence number
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateGrpSeq(InputStream input,
                                 @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int result = Content.updateGroupSeqNumber(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"result\":" + result + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Group sequence number.\"}")
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

    /***
     *  Remove content id from group content
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("remove")
    public Response updateGrpContents(InputStream input,
                                 @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int result = Content.removeGroupContentOfItem(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"result\":" + result + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Remove content from Item\"}")
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