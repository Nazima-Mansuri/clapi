package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Product;
import com.brewconsulting.DB.masters.SettingContent;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Created by lcom16 on 18/10/16.
 */
@Path("settingcontents")
@Secured
public class SettingContents {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Produces all contents.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("{divId}")
    @Secured
    public Response products(@PathParam("divId") int divId,@Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            resp = Response.ok(
                    mapper.writeValueAsString(SettingContent
                            .getAllContent(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get contents").build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /**
     * add content
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param
     */
     @POST
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createPro(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("contentName") String contentName,
            @FormDataParam("contentDesc") String contentDesc,
            @FormDataParam("divId") int divId,
            @Context ContainerRequestContext crc)  {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store image in AWS bucket.
                uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
            } else {
                uploadFilePath = null;
            }

            int contentid = SettingContent.addSettingContent(contentName, contentDesc,
                    divId, uploadFilePath, (LoggedInUser) crc.getProperty("userObject"));

            if (contentid != 0)
                resp = Response.ok("{\"id\":" + contentid + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Content")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create Content").build();
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
     * update content
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param contentDesc
     * @param divId
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateSettingContent(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("contentName") String contentName,
            @FormDataParam("contentDesc") String contentDesc,
            @FormDataParam("divId") int divId,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("id") int id,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;

        try {
            System.out.println("isUpdated : " + isUpdated);
            if (isUpdated) {
                if (fileFormDataContentDisposition != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store content in AWS bucket.
                    uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = null;
                }
            } else {
                uploadFilePath = url;
            }

            SettingContent.updateSettingContent(contentName, contentDesc,
                    divId, uploadFilePath, id, (LoggedInUser) crc.getProperty("userObject"));

            resp = Response.ok().build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update content").build();
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
     * delete content
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteSettingContent(@PathParam("id") Integer id,
                                         @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            int affectedRow = SettingContent.deleteSettingContent(id,
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
                    .entity("You are not authorized to delete content").build();
        } catch (PSQLException ex) {
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

}
