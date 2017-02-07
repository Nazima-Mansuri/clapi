package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.MeetingProcessContent;
import com.brewconsulting.DB.masters.SettingContent;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by lcom62_one on 1/12/2017.
 */

@Path("meetingprocesscontents")
@Secured
public class MeetingProcessContents {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(MeetingProcessContents.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /**
     * Produces all meeting process contents.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("{meetingId}/{agendaId}")
    @Secured
    public Response getAllMeetingProcessContents(@PathParam("meetingId") int meetingId,@PathParam("agendaId") int agendaId,
                                @Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(MeetingProcessContent
                            .getAllMeetingProcessContent(meetingId,agendaId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get meeting process contents\"}")
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
     *  Add meeting process contents.
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param contentName
     * @param contentDesc
     * @param divId
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createProcessCont(
            @FormDataParam("uploadFile") List<FormDataBodyPart> contents,
            /*@FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,*/
            @FormDataParam("meetingId") int meetingId,
            @FormDataParam("agendaId") int agendaId,
            @Context ContainerRequestContext crc)  {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = null;
        String fileType = "";
        List<String> filePath = new ArrayList<>();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

           /* if (fileFormDataContentDisposition != null) {
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
            }*/

            if(contents.size() > 0) {
                for (int i = 0; i < contents.size(); i++) {
                    FormDataContentDisposition fileFormDataContentDisposition = contents.get(i).getFormDataContentDisposition();

                    if (fileFormDataContentDisposition != null) {
                        if (fileFormDataContentDisposition.getFileName() != null) {

                            fileType = contents.get(i).getMediaType().toString();
                            String[] split = fileType.split("/");
                            fileType = split[0];

                            fileName = System.currentTimeMillis() + "_"
                                    + fileFormDataContentDisposition.getFileName();
                            // This method is used to store image in AWS bucket.
                            uploadFilePath = SettingContent.writeToFile(contents.get(i).getValueAs(InputStream.class), fileName);
                            filePath.add(uploadFilePath);

                        } else {
                            uploadFilePath = "";
                        }
                    } else {
                        uploadFilePath = "";
                    }
                }
            }

            List<Integer>  idList = MeetingProcessContent.addMeetingProcessContent(meetingId, agendaId,
                     filePath, (LoggedInUser) crc.getProperty("userObject"));

                resp = Response.ok("{\"idList\":" + idList + "}").build();
                System.out.println("Id List : " + idList);

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Meeting Process Content \"}")
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

    /***
     *  Update meeting process contents.
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param isUpdated
     * @param url
     * @param id
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateMeetingProcessContent(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
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

            int affectedRows = MeetingProcessContent.updateMeetingProcessContent(uploadFilePath,
                    id, (LoggedInUser) crc.getProperty("userObject"));

            if (affectedRows != 0)
                resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Update Meeting Process Content")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update meeting process contents\"}")
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


    /***
     *  delete meeting process contents.
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteMeetingProcessContent(@PathParam("id") Integer id,
                                         @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = MeetingProcessContent.deleteMeetingProcessContent(id,
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
                    .entity("{\"Message\":" + "\"You are not authorized to delete meeting process contents\"}")
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
