package com.brewconsulting.DB.masters;

import com.brewconsulting.PushRaven.FcmResponse;
import com.brewconsulting.PushRaven.Notification;
import com.brewconsulting.PushRaven.Pushraven;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by lcom53 on 21/12/16.
 */
@Path("/sendPush")
public class PushNotification {

    /***
     *
     *
     * @param input
     * @return
     * @throws IOException
     */
    @POST
    @Secured
    @Path("push")
    @Produces("application/json")
    public Response create(InputStream input, @Context ContainerRequestContext crc) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(input);

        String serverKey = "AAAAy-Ycr-M:APA91bFkHHd6lMRTasEqaRepH9iWZHT9alFzSPPl7ZLd3PCeIFlKx1m-TRhZyPfAsr8kXNp_96YzPoS8HqSveg7jvtfj6C6-s5lY-EsRp-sL01CDhUCwg3V1iGR9OwhblI7LMwm0IUys-rKFObq3VA-vCrfCMDgoaQ";
//        String deviceId = node.get("DeviceId").asText();

        String deviceToken = null;
        String deviceOS = null;
        int userid = 0;

        try {

            String device = User.getDeviceDetails(userid);
            String[] deviceDetails = device.split(",");

            for (int i=0;i<deviceDetails.length;i++)
            {
                deviceToken = deviceDetails[0];
                deviceOS = deviceDetails[1];
            }

            System.out.println("Device Token : " + deviceToken);
            System.out.println("Device OS : " + deviceOS);

            if(deviceOS.equalsIgnoreCase("Android")) {

                Pushraven.setKey(serverKey);

                Notification raven = new Notification();
                raven.title("MyTitle")
                        .text("Hello World!")
                        .color("#ff0000 ")
                        .icon("icon")
                        .to(deviceToken);

                FcmResponse resp = Pushraven.push(raven);
                System.out.println("Response : " + resp);
            }

            if(deviceOS.equalsIgnoreCase("iOS"))
            {
                ApnsService service =
                        APNS.newService()
                                .withCert(getClass().getClassLoader().getResourceAsStream("Rolla_APNS_Development.p12"), "lanetteam1")
                                .withSandboxDestination()
                                .build();

                String payload = APNS.newPayload().alertBody("Can't be simpler than this!").build();
                service.push(deviceToken, payload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok("Done").type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
