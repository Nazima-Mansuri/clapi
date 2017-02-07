package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.SettingContent;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
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
 * Created by lcom16 on 18/10/16.
 */
@Path("settingcontents")
@Secured
public class SettingContents {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(SettingContents.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

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
    public Response setcontents(@PathParam("divId") int divId,@Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(SettingContent
                            .getAllContent(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Contents\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     *  Produces content for specific division with null division And
     *  these contents not exist as specific agenda content.
     *
     * @param divId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("{divId}/{agendaId}")
    @Secured
    public Response getcontents(@PathParam("divId") int divId,@PathParam("agendaId") int agendaId,@Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(SettingContent
                            .getDivisionSpecificContent(divId,agendaId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Contents\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ",e);
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
    public Response createCont(
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (fileFormDataContentDisposition != null) {
                if(fileFormDataContentDisposition.getFileName() != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
                }else {
                    uploadFilePath = "";
                }

            } else {
                uploadFilePath = "";
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

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Contents \"}")
                    .type(MediaType.APPLICATION_JSON)
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (isUpdated) {
                if (fileFormDataContentDisposition != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store content in AWS bucket.
                    uploadFilePath = SettingContent.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = "";
                }
            } else {
                uploadFilePath = url;
            }

            SettingContent.updateSettingContent(contentName, contentDesc,
                    divId, uploadFilePath, id, (LoggedInUser) crc.getProperty("userObject"));

            resp = Response.ok().build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update contents\"}")
                    .type(MediaType.APPLICATION_JSON)
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
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = SettingContent.deleteSettingContent(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to delete contents\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
            logger.error("PSQLException ",ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

}
