package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.*;
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
 * Created by lcom53 on 17/12/16.
 */

@Path("questioncollections")
@Secured
public class QuestionCollections {
    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(QuestionCollections.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");


    /***
     *  Produces List of all Group or Cycle meeting Question Collections.
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{agendaId}")
    public Response getquestioncollections(@QueryParam("isGroup") boolean isGroup,@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.collectionView.class).writeValueAsString(QuestionCollection
                            .getAllQuestionCollection(agendaId,isGroup, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Collections\"}")
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
     *  Produces List of all Group or Cycle meeting Question Collections Set.
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("set/{agendaId}")
    @Secured
    public Response getquestioncollectionsSet(@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.quesSetView.class).writeValueAsString(QuestionCollection
                            .getQuestionSet(agendaId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }
        catch (BadRequestException e) {
            logger.error("BadRequestException", e);
            resp = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"Message\":" + "\" You are not authorized for this test right now.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Collections\"}")
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
     *  Produces List of all Group or Cycle meeting Question Collections.
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("settings/{agendaId}")
    public Response getcollectionsSettings(@QueryParam("isGroup") boolean isGroup,@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.settingView.class).writeValueAsString(QuestionCollection
                            .getSettingDetails(agendaId,isGroup, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Collections\"}")
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
     * Add new Question collection.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createQuesCollection(InputStream input,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int collectionId = QuestionCollection.addQuestionCollection(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"id\":" + collectionId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Question Collection\"}")
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
     * Delete Question collection
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteQuesCollection(@PathParam("id") Integer id,@QueryParam("isGroup") boolean isGroup,
                               @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = QuestionCollection.deleteQuestionCollections(id,isGroup,
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
                    .entity("{\"Message\":" + "\"You are not authorized to delete Questions Collections\"}")
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
     *  Remove Question Id from group Question Collections or Cyclemeeting Quetion Collections
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
    public Response updateQuestions(InputStream input,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = QuestionCollection.removeQuestionFromCollection(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Remove Questions from Collections\"}")
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
     *  Append question id in group question collection or cyclemeeting question collections.
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("append")
    public Response appendQuestions(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = QuestionCollection.appendQuestionInCollection(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Append Questions In Collections\"}")
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
     *  Update Collection Details.
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateQuesCollections(InputStream input,
                                    @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = QuestionCollection.updateCollections(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update Question Collections \"}")
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
     *  Update Collection Settings..
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("settings")
    public Response updateQuesSettings(InputStream input,
                                          @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRows = QuestionCollection.updateSettings(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update Question Collections Settings \"}")
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
     *  Add User's all answer at a time.
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("allanswers")
    public Response addAllAnswers(InputStream input,
                                         @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int totalAffectedRows = QuestionCollection.addUsersAllAnswer(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"totalAffectedRows\":" + totalAffectedRows + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Add user's all answers.\"}")
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
     *  Add User's Single answer.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("singleanswer")
    public Response addSingleAnswer(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int resultId = QuestionCollection.addUsersAnswer(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"resultId\":" + resultId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add User answer. \"}")
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
     *  Produces User's Total Score
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("userwiseresult/{agendaId}")
    public Response getUserWiseResult(@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getUsersWiseResult(agendaId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Total Score\"}")
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
     *  Produces User's Total Score
     *
     * @param agendaId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("questionwiseresult/{agendaId}")
    public Response getQuestionWiseResult(@PathParam("agendaId") int agendaId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getQuestionWiseResult(agendaId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Total Score\"}")
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
     *  Produces User's Questionwise Score.
     *
     * @param userId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("questionwisescore/{userId}")
    public Response getQuestionWiseScore(@PathParam("userId") int userId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getUsersQuestionWiseScore(userId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Score \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    @GET
    @Produces("application/json")
    @Secured
    @Path("usersparticularscore/{userId}/{agendaId}/{mode}")
    public Response getUsersParticalurScore(@PathParam("userId") int userId,@PathParam("agendaId") int agendaId,
                                            @PathParam("mode") String mode,@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            System.out.println("Mode : " + mode);
            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getUserWiseParticularScore(userId,agendaId,mode, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Score \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    @GET
    @Produces("application/json")
    @Secured
    @Path("questionwiseuserstatus/{questionId}/{agendaId}/{mode}")
    public Response getQuestionWiseUserStatus(@PathParam("questionId") int questionId,@PathParam("agendaId") int agendaId,
                                            @PathParam("mode") String mode,@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getQuestionWiseUserStatus(questionId,agendaId,mode, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Score \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    @GET
    @Produces("application/json")
    @Secured
    @Path("userstotalscore/{userId}/{agendaId}")
    public Response getUserTotalScore(@PathParam("userId") int userId,@PathParam("agendaId") int agendaId,
                                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(QuestionCollection
                            .getUserTotalScore(userId,agendaId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Questions Score \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }
}
