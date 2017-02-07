package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Question;
import com.brewconsulting.DB.masters.QuestionCollection;
import com.brewconsulting.DB.masters.User;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.omg.CORBA.PUBLIC_MEMBER;
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
 * Created by lcom53 on 7/11/16.
 */
@Path("questions")
@Secured
public class Questions {

    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Questions.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces all questions.
     *
     * @param divId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{divId}")
    public Response questions(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Question
                            .getAllQuestions(divId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /**
     *  Search question
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    @Path("filter")
    public Response filter(InputStream input,
                           @Context ContainerRequestContext crc)
    {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);

            resp = Response.ok(
                    mapper.writeValueAsString(Question
                            .filterQuestions(node, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        }
        catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Seach Questions\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     * Add Question
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param questionText
     * @param pollable
     * @param division
     * @param questionType
     * @param complexityLevel
     * @param Product
     * @param keywords
     * @param feedbackRight
     * @param feedbackWrong
     * @param isActive
     * @param questionJson
     * @param correctAnswer
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addQues(@FormDataParam("questionImage") InputStream fileInputStream,
                            @FormDataParam("questionImage") FormDataContentDisposition fileFormDataContentDisposition,
                            @FormDataParam("questionImage") final FormDataBodyPart body,
                            @FormDataParam("questionText") String questionText,
                            @FormDataParam("pollable") boolean pollable,
                            @FormDataParam("division") int division,
                            @FormDataParam("questionType") String questionType,
                            @FormDataParam("complexityLevel") String complexityLevel,
                            @FormDataParam("Product") String Product,
                            @FormDataParam("keywords") String keywords,
                            @FormDataParam("feedbackRight") String feedbackRight,
                            @FormDataParam("feedbackWrong") String feedbackWrong,
                            @FormDataParam("isActive") boolean isActive,
                            @FormDataParam("questionJson") String questionJson,
                            @FormDataParam("correctAnswer") String correctAnswer,
                            @FormDataParam("isReview") boolean isReview,
                            @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = "";
        String fileType = "";

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (fileFormDataContentDisposition != null) {
                if (fileFormDataContentDisposition.getFileName() != null) {

                    fileType = body.getMediaType().toString();
                    String[] split = fileType.split("/");
                    fileType = split[0];

                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = Question.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = "";
                }
            } else {
                uploadFilePath = "";
            }

            int questionId = Question.insertQuestion(questionText, pollable, division, questionType,
                    complexityLevel, Product, keywords, feedbackRight, feedbackWrong, isActive, questionJson, correctAnswer,
                    uploadFilePath, fileType,isReview, (LoggedInUser) crc.getProperty("userObject"));

            if (questionId != 0)
                resp = Response.ok("{\"id\":" + questionId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Question")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Insert Question. \"}")
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
     * Update Question
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param questionText
     * @param pollable
     * @param division
     * @param questionType
     * @param complexityLevel
     * @param Product
     * @param keywords
     * @param feedbackRight
     * @param feedbackWrong
     * @param isActive
     * @param questionJson
     * @param correctAnswer
     * @param questionId
     * @param isUpdated
     * @param url
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateQues(@FormDataParam("questionImage") InputStream fileInputStream,
                               @FormDataParam("questionImage") FormDataContentDisposition fileFormDataContentDisposition,
                               @FormDataParam("questionText") String questionText,
                               @FormDataParam("questionImage") final FormDataBodyPart body,
                               @FormDataParam("pollable") boolean pollable,
                               @FormDataParam("division") int division,
                               @FormDataParam("questionType") String questionType,
                               @FormDataParam("complexityLevel") String complexityLevel,
                               @FormDataParam("Product") String Product,
                               @FormDataParam("keywords") String keywords,
                               @FormDataParam("feedbackRight") String feedbackRight,
                               @FormDataParam("feedbackWrong") String feedbackWrong,
                               @FormDataParam("isActive") boolean isActive,
                               @FormDataParam("questionJson") String questionJson,
                               @FormDataParam("correctAnswer") String correctAnswer,
                               @FormDataParam("questionId") int questionId,
                               @FormDataParam("isUpdated") boolean isUpdated,
                               @FormDataParam("url") String url,
                               @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = "";
        String fileType = "";
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (isUpdated) {
                if (fileFormDataContentDisposition != null) {
                    if (fileFormDataContentDisposition.getFileName() != null) {

                        fileType = body.getMediaType().toString();
                        String[] split = fileType.split("/");
                        fileType = split[0];

                        fileName = System.currentTimeMillis() + "_"
                                + fileFormDataContentDisposition.getFileName();
                        // This method is used to store image in AWS bucket.
                        uploadFilePath = Question.writeToFile(fileInputStream, fileName);
                    }
                }
            } else {
                uploadFilePath = url;
            }

            int affectedRows = Question.updateQuestion(questionText, pollable, division, questionType,
                    complexityLevel, Product, keywords, feedbackRight, feedbackWrong, isActive, questionJson, correctAnswer,
                    uploadFilePath,fileType, questionId, (LoggedInUser) crc.getProperty("userObject"));

            if (affectedRows != 0)
                resp = Response.ok("{\"id\":" + affectedRows + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Update Question")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update Question. \"}")
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
     * Delete Question
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteQues(@PathParam("id") Integer id,
                               @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = Question.deleteQuestions(id,
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
                    .entity("{\"Message\":" + "\"You are not authorized to delete Question\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
            logger.error(" PSQLException ", ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(" Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Clonbe question.
     *
     * @param id
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("{id}")
    public Response cloneQuestion(@PathParam("id") int id,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int questionId = Question.cloneQuestion(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + questionId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to clone Question\"}")
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
}
