package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeetingTerritory;
import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.PushRaven.FcmResponse;
import com.brewconsulting.PushRaven.Notification;
import com.brewconsulting.PushRaven.Pushraven;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;

/**
 * Created by lcom53 on 3/11/16.
 */

@Path("cyclemeetingterritories")
@Secured
public class CycleMeetingTerritories {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(CycleMeetingTerritories.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     *  Produces Cyclemeeting Territories
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response meetingsTerr(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllCycleMeetingTerr(id, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cycle meeting Territories\"}")
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
     * Produces feedschedule user details.
     *
     * @param feedScheduleId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("feedscheduleusers/{feedScheduleId}")
    public Response feedScheduleUserDetails(@PathParam("feedScheduleId") int feedScheduleId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllUserDetailsOfFeedSchedule(feedScheduleId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Feed schedule user details.\"}")
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
     * @param feedDeliveryId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("feeddeliveryusers/{feedDeliveryId}")
    public Response feedDeliveryUserDetails(@PathParam("feedDeliveryId") int feedDeliveryId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllUserDetailsOfFeedDelivery(feedDeliveryId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Feed schedule user details.\"}")
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
     *  Produces Assesment user details.
     *
     * @param assesmentId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("assesmentusers/{assesmentId}")
    public Response assesmentUserDetails(@PathParam("assesmentId") int assesmentId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllUserDetailsOfAssesment(assesmentId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Assessment user details.\"}")
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
     *  Add Cyclemeeting Territory
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createMeetingTerr(InputStream input,
                                      @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int count = CycleMeetingTerritory.addCycleMeetingTerr(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"Count\":" + count + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Cyclemeeting Territory\"}")
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
     *  Add Attendace
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("attend")
    public Response createMeetingAtten(InputStream input,
                                       @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int attendanceId = CycleMeetingTerritory.addCycleMeetingAttendance(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"attendanceId\":" + attendanceId + "}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Cyclemeeting Attendance\"}")
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
     *  Add Session Actual Time ot Session Start Time.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("session")
    public Response createSessionActualTime(InputStream input,
                                            @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);

            List<String> sessionList = CycleMeetingTerritory.addSessionStartTime(node,
                    (LoggedInUser) crc.getProperty("userObject"));

            int id = Integer.parseInt(sessionList.get(0));
            String startTime = sessionList.get(1);

            System.out.println(" ID : " + id);
            System.out.println(" Date : " + startTime);

            resp = Response.ok("{\"sessionStartTimeId\":" + id + ",\"sessionStartTime\":\" " + startTime + "\"}").build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to create Session Actual Time\"}")
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
     *  Update Session End Time When Session is ended.
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("session")
    public Response updateDiv(InputStream input,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            CycleMeetingTerritory.updateSessionEndTime(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Division\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException", e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception", e);
            // TODO Auto-generated catch block
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Send Push notification to user when exam started.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("notification/{meetingId}")
    public Response sendnotification(@PathParam("meetingId") int meetingId, @Context ContainerRequestContext crc) {
        Response resp = null;
        String serverKey = "AAAAy-Ycr-M:APA91bFkHHd6lMRTasEqaRepH9iWZHT9alFzSPPl7ZLd3PCeIFlKx1m-TRhZyPfAsr8kXNp_96YzPoS8HqSveg7jvtfj6C6-s5lY-EsRp-sL01CDhUCwg3V1iGR9OwhblI7LMwm0IUys-rKFObq3VA-vCrfCMDgoaQ";
        String deviceToken = "";
        String deviceOS = "";
        String firstName = "" ;
        String lastName="";
        String deviceDetails;
        String arr[];
        ArrayList<String> list = new ArrayList<>();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            List<String> allDetails = CycleMeetingTerritory
                    .sendNotification(meetingId, (LoggedInUser) crc
                            .getProperty("userObject"));

            int agendaId = CycleMeetingTerritory.getAgendaIdFromMeetingId(meetingId, (LoggedInUser) crc
                    .getProperty("userObject"));

            System.out.println("Agenda Id : " + agendaId);

            System.out.println("Device List Size : " + allDetails.size());
            for (int i = 0; i < allDetails.size(); i++) {
                deviceDetails = allDetails.get(i);
                arr = deviceDetails.split(",");
                deviceToken = arr[0];
                deviceOS = arr[1];
                firstName = arr[2];
                lastName = arr[3];

                System.out.println(" Device Token : " + deviceToken);
                System.out.println(" Device OS : " + deviceOS);

                if ((!deviceToken.isEmpty() && !deviceOS.isEmpty()) &&
                        (deviceToken!=null && deviceOS!=null) &&
                        (!deviceToken.equalsIgnoreCase("null") &&
                                !deviceOS.equalsIgnoreCase("null"))) {

                    if (deviceOS.equalsIgnoreCase("Android")) {
                        Pushraven.setKey(serverKey);

                        Notification raven = new Notification();
                        raven.title("Rolla App")
                                .body("Exam Started.!")
                                .action("Test")
                                .testId(agendaId)
                                .icon("icon")
                                .to(deviceToken);

                        System.out.println("Raven : " + raven.toJSON());

                        FcmResponse fcmresp = Pushraven.push(raven);
                        System.out.println("\n Response : " + fcmresp);
                        resp = Response.ok().
                                entity("{\"Message\":" + "\"Notification Send Successfully.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
                    }

                    if (deviceOS.equalsIgnoreCase("iOS")) {
                        ApnsService service =
                                APNS.newService()
                                        .withCert(getClass().getClassLoader().getResourceAsStream("Rolla_Production.p12"), "lanetteam1")
                                        .withProductionDestination()
                                        .build();
                        /*ApnsService service =
                                APNS.newService()
                                        .withCert(getClass().getClassLoader().getResourceAsStream("Rolla_Development.p12"), "lanetteam1")
                                        .withSandboxDestination()
                                        .build();*/

                        String payload = APNS.newPayload().alertBody("Exam Started.!").alertAction("Test").customField("id",agendaId).build();
                        System.out.println("Payload : " + payload);
                        ApnsNotification apnresp = service.push(deviceToken, payload);
                        resp = Response.ok().
                                entity("{\"Message\":" + "\"Notification Send Successfully.\"}")
                                .type(MediaType.APPLICATION_JSON).build();
                    }
                }
                else
                {
                   resp = Response.accepted().
                            entity("{\"Message\":" + "\"" + firstName + " "+ lastName + "\"}")
                            .type(MediaType.APPLICATION_JSON).build();
                   list.add(resp.getEntity().toString());
                }
            }
            if(list.size() > 0)
                return Response.ok().entity(list).type(MediaType.APPLICATION_JSON).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cycle meeting Territories\"}")
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
     *  Produce
     *
     * @param meetingId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("attendee/{meetingId}")
    public Response getAllAttendee(@PathParam("meetingId") int meetingId, @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeetingTerritory
                            .getAllAttendee(meetingId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cycle meeting Territories\"}")
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
