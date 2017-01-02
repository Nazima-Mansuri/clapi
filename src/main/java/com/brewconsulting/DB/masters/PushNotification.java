package com.brewconsulting.DB.masters;

import com.brewconsulting.PushRaven.FcmResponse;
import com.brewconsulting.PushRaven.Notification;
import com.brewconsulting.PushRaven.Pushraven;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lcom53 on 21/12/16.
 */
@Path("/sendPush")
public class PushNotification {

    @POST
    @Secured
    @Path("push")
    @Produces("application/json")
    public Response create(InputStream input) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(input);

        String serverKey = "AAAAFrkT4BI:APA91bGzh4lHb3i56o0gXaPd1kB6UNFo31Sm_HYeQuQqBygKWvaITrzEpQ8zZ3WsHcbPlUOWyQCvfC8gf7wUj-THAJmWqGZf7G8DbSL6HGRdreXD7QlW9hr8bfHWcCg8z43KWOD74mToKexz3sTp2h69209LomtCsQ";
        String deviceId = node.get("DeviceId").asText();

        try {

            Pushraven.setKey(serverKey);

            Notification raven = new Notification();
            raven.title("MyTitle")
                    .text("Hello World!")
                    .color("#ff0000")
                    .icon("icon")
                    .to(deviceId);

             FcmResponse resp = Pushraven.push(raven);
            System.out.println("Response : " + resp);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok("Done").type(MediaType.TEXT_PLAIN_TYPE).build();
    }

}
