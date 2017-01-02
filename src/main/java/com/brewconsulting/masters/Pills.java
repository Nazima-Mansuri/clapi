package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Pill;
import com.brewconsulting.DB.masters.Question;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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

    @POST
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addQues(@FormDataParam("pillImage") List<FormDataBodyPart> pillimages,
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

        try {

            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if(pillimages.size() > 0) {
                for (int i = 0; i < pillimages.size(); i++) {

                    FormDataContentDisposition fileFormDataContentDisposition = pillimages.get(i).getFormDataContentDisposition();

                    if (fileFormDataContentDisposition != null) {
                        if (fileFormDataContentDisposition.getFileName() != null) {

                            fileType = pillimages.get(i).getMediaType().toString();
                            String[] split = fileType.split("/");
                            fileType = split[0];

                            System.out.println("FIle Type : " + fileType);
                            fileName = System.currentTimeMillis() + "_"
                                    + fileFormDataContentDisposition.getFileName();
                            // This method is used to store image in AWS bucket.
                            uploadFilePath = Question.writeToFile(pillimages.get(i).getValueAs(InputStream.class), fileName);
                            filePath.add(uploadFilePath);
                            System.out.println("File Path I : " + uploadFilePath);
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
                    filePath,(LoggedInUser) crc.getProperty("userObject"));

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

}
