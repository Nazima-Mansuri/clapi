package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.QuestionCollection;
import com.brewconsulting.DB.masters.Score;
import com.brewconsulting.DB.masters.UserViews;
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
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by lcom62_one on 1/6/2017.
 */

@Path("scores")
@Secured
public class Scores {
    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Scores.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     * Produces session details in that session is started or ended.
     *
     * @param meetingId
     * @param sessionId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("sessiondetails/{meetingId}/{sessionId}")
    public Response getSessionDetails(@PathParam("meetingId") int meetingId, @PathParam("sessionId") int sessionId,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.sessionView.class).writeValueAsString(Score
                            .getSessionDetails(meetingId, sessionId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Session details.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

/*
    *//***
     *  Add user's all answers at a time.
     *
     * @param input
     * @param crc
     * @return
     *//*
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
            int totalAffectedRows = Score.addUsersAllAnswer(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"totalAffectedRows\":" + totalAffectedRows + "}").build();
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

    *//***
     *   Add User's Single Answer at a time.
     *
     * @param input
     * @param crc
     * @return
     *//*
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
            int resultId = Score.addUsersAnswer(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"resultId\":" + resultId + "}").build();
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

    *//***
     *  Produces User wise result.
     *
     * @param agendaId
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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

    *//***
     *  Produces Question wise result.
     *
     * @param agendaId
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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

    *//***
     *  Produces User's Questionwise Score.
     *
     * @param userId
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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

    *//***
     *  Produces User's particular score.
     *
     * @param userId
     * @param agendaId
     * @param mode
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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

    *//***
     *  Produces Question wise User's status.
     *
     * @param questionId
     * @param agendaId
     * @param mode
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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

    */

    /***
     *  Produces User's Total Score.
     *
     * @param userId
     * @param agendaId
     * @param crc
     * @return
     *//*
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
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
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
    }*/
    @GET
    @Produces("application/json")
    @Secured
    @Path("usersallexamscores")
    public Response getUsersAllExamsScore(@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.scoreView.class).writeValueAsString(Score
                            .getUsersAllExamsScore((LoggedInUser) crc
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
