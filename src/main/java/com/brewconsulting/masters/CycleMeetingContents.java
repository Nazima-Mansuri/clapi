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
import java.util.Properties;

/**
 * Created by lcom53 on 9/11/16.
 */
@Path("cyclemeetingcontents")
@Secured
public class CycleMeetingContents {
    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(CycleMeetingContents.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces List of Cyclemeeting content
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
    public Response cyclemeetingContents(@PathParam("meetingId") int meetingId ,@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getAllCycleMeetingContents(meetingId,divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get division").build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     *  Produces List of Group Contents By Specific Agenda
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{agendaId}")
    public Response cyclemeetingContents(@PathParam("agendaId") int agendaId , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getChildContentByAgenda(agendaId,(LoggedInUser) crc
                                    .getProperty("userObject")) )).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get division").build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }

        return resp;
    }


    /***
     *  Add New CycleMeeting Content
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param contentSeq
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
            @FormDataParam("contentSeq") int contentSeq,
            @FormDataParam("agendaId") int agendaId,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (fileFormDataContentDisposition != null) {
             if(fileFormDataContentDisposition.getFileName() != null) {
                 fileName = System.currentTimeMillis() + "_"
                         + fileFormDataContentDisposition.getFileName();
                 // This method is used to store content in AWS bucket.
                 uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
             }
             else {
                 uploadFilePath = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
             }
            } else {
                uploadFilePath = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
            }

            int contentId = Content.addCycleMeetingContent(contentName, contentDesc, contentType,
                    divId,uploadFilePath,agendaId,(LoggedInUser) crc.getProperty("userObject"));

            if (contentId != 0)
                resp = Response.ok("{\"id\":" + contentId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Product")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create product").build();
        } catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Add Exixting Content in CycleMeetingContent
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createExistMeetingContent(InputStream input,
                                          @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = Content.addExistingCycleMeetingContent(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + affectedRows + "}").build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create division")
                    .build();
        } catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteMeetingContent(@PathParam("id") Integer id,
                                     @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            // affectedRow given how many rows deleted from database.
            int affectedRow = Content.deleteCycleMeetingContent(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Delete Cyclemeeting Content\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (PSQLException ex) {
            logger.error("PSQLException ",ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
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
    public Response updateChildSeq(InputStream input,
                                 @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int result = Content.updateMeetingSeqNumber(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"result\":" + result + "}").build();
        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Division\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}
