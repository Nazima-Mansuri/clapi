package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.Content;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.SettingContent;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lcom53 on 18/10/16.
 */

@Path("groupcontents")
@Secured
public class GroupContents {

    ObjectMapper mapper = new ObjectMapper();


    /***
     *  Produces List of Group Contents
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
    public Response groupContents(@PathParam("meetingId") int meetingId ,@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getAllGroupContents(meetingId,divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to get group meeting contents \"}")
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
    public Response groupContents(@PathParam("agendaId") int agendaId , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            resp = Response.ok(
                    mapper.writeValueAsString(Content
                            .getGroupContentByAgenda(agendaId,(LoggedInUser) crc
                                    .getProperty("userObject")) )).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to get group content\"}")
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
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;

        try {

            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store content in AWS bucket.
                uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
            } else {
                uploadFilePath = null;
            }

            int contentId = Content.addGroupContent(contentName, contentDesc, contentType,
                    divId,uploadFilePath,agendaId,(LoggedInUser) crc.getProperty("userObject"));

            if (contentId != 0)
                resp = Response.ok("{\"id\":" + contentId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Product")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to add group meeting content\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (IOException e) {
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

    /***
     *  Add Exixting Content in Group Content
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
            JsonNode node = mapper.readTree(input);
            int affectedRows = Content.addExistingGroupContent(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to add Content from Existing content \"}")
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

    /***
     * update Group content
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param contentSeq
     * @param agendaId
     * @param id
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateGroupContent(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("contentName") String contentName,
            @FormDataParam("contentDesc") String contentDesc,
            @FormDataParam("contentType") String contentType,
            @FormDataParam("divId") int divId,
            @FormDataParam("contentSeq") int contentSeq,
            @FormDataParam("agendaId") int agendaId,
            @FormDataParam("id") int id,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;

        try {

            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store content in AWS bucket.
                uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
            } else {
                uploadFilePath = null;
            }

            Content.updateGroupContent(contentName, contentDesc, contentType,
                    divId,uploadFilePath,contentSeq,agendaId,(LoggedInUser) crc.getProperty("userObject"),id);

            resp = Response.ok().build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"Message\":" + "\"You are not authorized to update group meeting content\"}")
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