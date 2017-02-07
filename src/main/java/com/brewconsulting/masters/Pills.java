package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Pill;
import com.brewconsulting.DB.masters.Question;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
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
 * Created by lcom53 on 29/12/16.
 */

@Path("pills")
@Secured
public class Pills {

    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Pills.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    public Response getAllPills(@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .getAllPills((LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Pills \"}")
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
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("pillbyid/{pillId}")
    public Response getPillById(@PathParam("pillId") int pillId,@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .getPillById(pillId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Pills \"}")
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
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{divId}")
    public Response getAllPillsByDivId(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .getAllPillsByDivision(divId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Pills \"}")
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
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("userpills/{divId}")
    public Response getAllPillsByUser(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .getAllPillsOfUser(divId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Pills \"}")
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
     * @param feedId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("feedpills/{feedId}")
    public Response getAllPillsOfFeed(@PathParam("feedId") int feedId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .getAllPillsOfFeed(feedId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Pills \"}")
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
     *
     * @param pillimages
     * @param divid
     * @param title
     * @param body
     * @param questionType
     * @param questiontext
     * @param answeroptions
     * @param answertext
     * @param scorecorrect
     * @param scoreincorrect
     * @param products
     * @param keywords
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addPills(@FormDataParam("pillImage") List<FormDataBodyPart> pillimages,
                             @FormDataParam("divid") int divid,
                             @FormDataParam("title") String title,
                             @FormDataParam("body") String body,
                             @FormDataParam("questionType") String questionType,
                             @FormDataParam("questiontext") String questiontext,
                             @FormDataParam("answeroptions") String answeroptions,
                             @FormDataParam("answertext") String answertext,
                             @FormDataParam("scorecorrect") int scorecorrect,
                             @FormDataParam("scoreincorrect") String scoreincorrect,
                             @FormDataParam("products") String products,
                             @FormDataParam("keywords") String keywords,
                             @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = "";
        String fileType = "";
        List<String> filePath = new ArrayList<>();
        List<String> fileTypes = new ArrayList<>();

        try {

            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (pillimages.size() > 0) {
                for (int i = 0; i < pillimages.size(); i++) {

                    FormDataContentDisposition fileFormDataContentDisposition = pillimages.get(i).getFormDataContentDisposition();

                    if (fileFormDataContentDisposition != null) {
                        if (fileFormDataContentDisposition.getFileName() != null) {

                            fileType = pillimages.get(i).getMediaType().toString();
                            String[] split = fileType.split("/");
                            fileType = split[0];
                            fileTypes.add(fileType);

                            fileName = System.currentTimeMillis() + "_"
                                    + fileFormDataContentDisposition.getFileName();
                            // This method is used to store image in AWS bucket.
                            uploadFilePath = Question.writeToFile(pillimages.get(i).getValueAs(InputStream.class), fileName);
                            System.out.println("FILE PATH : " + uploadFilePath);
                            filePath.add(uploadFilePath);
                            System.out.println("SIZE : " + filePath.size());

                        } else {
                            uploadFilePath = null;
                        }
                    } else {
                        uploadFilePath = null;
                    }
                }
            }

            int pillId = Pill.addPills(divid, title, body, questionType,
                    questiontext, answeroptions, answertext, scorecorrect, scoreincorrect, products, keywords,
                    filePath, fileTypes, (LoggedInUser) crc.getProperty("userObject"));

            if (pillId != 0)
                resp = Response.ok("{\"id\":" + pillId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Question")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Insert Pill. \"}")
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
     *
     * @param pillimages
     * @param divid
     * @param title
     * @param body
     * @param questionType
     * @param questiontext
     * @param answeroptions
     * @param answertext
     * @param scorecorrect
     * @param scoreincorrect
     * @param products
     * @param keywords
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updatePills(@FormDataParam("pillImage") List<FormDataBodyPart> pillimages,
                                @FormDataParam("divid") int divid,
                                @FormDataParam("title") String title,
                                @FormDataParam("body") String body,
                                @FormDataParam("questionType") String questionType,
                                @FormDataParam("questiontext") String questiontext,
                                @FormDataParam("answeroptions") String answeroptions,
                                @FormDataParam("answertext") String answertext,
                                @FormDataParam("scorecorrect") int scorecorrect,
                                @FormDataParam("scoreincorrect") String scoreincorrect,
                                @FormDataParam("products") String products,
                                @FormDataParam("keywords") String keywords,
                                @FormDataParam("isUpdate") boolean isUpdate,
                                @FormDataParam("id") int id,
                                @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = "";
        String fileType = "";
        List<String> filePath = new ArrayList<>();
        List<String> fileTypes = new ArrayList<>();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (isUpdate) {
                if (pillimages.size() > 0) {
                    for (int i = 0; i < pillimages.size(); i++) {

                        FormDataContentDisposition fileFormDataContentDisposition = pillimages.get(i).getFormDataContentDisposition();

                        if (fileFormDataContentDisposition != null) {
                            if (fileFormDataContentDisposition.getFileName() != null) {
                                fileType = pillimages.get(i).getMediaType().toString();
                                String[] split = fileType.split("/");
                                fileType = split[0];
                                fileTypes.add(fileType);

                                System.out.println("FIle Type : " + fileType);
                                fileName = System.currentTimeMillis() + "_"
                                        + fileFormDataContentDisposition.getFileName();
                                // This method is used to store image in AWS bucket.
                                uploadFilePath = Question.writeToFile(pillimages.get(i).getValueAs(InputStream.class), fileName);
                                filePath.add(uploadFilePath);
                            } else {
                                uploadFilePath = null;
                            }
                        } else {
                            uploadFilePath = null;
                        }
                    }
                }
            }

            int affectedRows = Pill.updatePills(divid, title, body, questionType,
                    questiontext, answeroptions, answertext, scorecorrect, scoreincorrect, products, keywords,
                    filePath, fileTypes, isUpdate, id, (LoggedInUser) crc.getProperty("userObject"));

            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update Pill. \"}")
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
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    @Path("filter")
    public Response filterPill(InputStream input,
                               @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);

            resp = Response.ok(
                    mapper.writeValueAsString(Pill
                            .filterPills(node, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Seach Pills.\"}")
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
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deletePill(@PathParam("id") Integer id,
                               @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);
            // affectedRow given how many rows deleted from database.
            int affectedRow = Pill.deletePills(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).entity("{\"Message\":\" + \"\"Pill is not deleted.\"}").build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Delete Pill\"}")
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
            logger.error("Exception", e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }
}
