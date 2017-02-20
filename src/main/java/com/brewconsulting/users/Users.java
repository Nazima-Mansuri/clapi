package com.brewconsulting.users;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Role;
import com.brewconsulting.DB.masters.User;
import com.brewconsulting.DB.masters.UserViews;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.brewconsulting.masters.Divisions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.Properties;

@Path("users")
@Secured
public class Users {
    static final Logger logger = Logger.getLogger(Users.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     * Get the details of the user. User profile, and roles he is in.
     *
     * @param id
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("{id}")
    @Secured
    public Response user(@PathParam("id") Integer id,
                         @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User.getProfile(
                            (LoggedInUser) crc.getProperty("userObject"), id))).build();
        }
        catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to view other's profile\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
            logger.error("Exception ",e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getStackTrace() + "\"}").build();
            e.printStackTrace();
        }

        return resp;

    }

    /***
     * Produces a List of Users which are not associate to any Territory.
     *
     * @param crc
     * @return
     */

    @GET
    @Produces("application/json")
    @Secured
    @Path("deassociateuser/{divId}")

    public Response daassUser(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.deAssociateView.class).writeValueAsString(User
                            .getDeassociateUser(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Deassociate User\"}")
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
     *  Get all divisions of logged in user
     *
     * @param userId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("divisions/{userId}")

    public Response getdivisions(@PathParam("userId") int userId, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.divView.class).writeValueAsString(User
                            .getDivisions(userId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Divisions of User\"}")
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
     * Produces a List of All Users.
     *
     * @param crc
     * @return
     */

    @GET
    @Produces("application/json")
    @Secured
    @Path("allUser")
    public Response getAllUsers(@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllUsers((LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
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
     * Produces a List of All Users With specific Division.
     *
     * @param crc
     * @return
     */

    @GET
    @Produces("application/json")
    @Secured
    @Path("allUser/{id}")
    public Response getAllUsersByDivisions(@PathParam("id") int id,@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllUsersByDivId(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
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
     * Produces list of Activate users for specific Division.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("userbydivision/{id}")
    public Response getAllActivateUsersByDivId(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllActivateUsersByDivId(id, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
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
     * Produces list of ROOT level Users.
     *
     * @param divId
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("rootlevelusers/{divId}")
    public Response getAllRootLevelUsers(@PathParam("divId") int divId, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllRootLevelUser(divId, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
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
     * Produces all Roles
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Path("roles")
    @Secured
    public Response roles(@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Role.getAllRoles(
                            (LoggedInUser) crc.getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Roles\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception " ,e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;

    }

    /***
     * Produces all clients which are active
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("clients")
    public Response getClients(@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writerWithView(UserViews.clientView.class).writeValueAsString(User
                            .getAllClients((LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Clients. \"}")
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
     * Deactivate user
     *
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Path("deactivate")
    public Response deactivateUser(InputStream input,
                                   @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRow = User.deactivateUser(node, (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }  catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Deactivate User \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Change password of user.
     *
     * @param input
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updatePassword(InputStream input) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRow = User.changePassword(node);
            resp = Response.ok("{\"affectedRow\":" + affectedRow + "}").build();
        }
        catch (InputMismatchException e) {
            logger.error("InputMismatchException ",e);
            if (resp == null)
                resp = Response.status(Response.Status.BAD_REQUEST).entity("{\"Message\":" + "\" Password Does not match. \"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        }
        catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Change password of User When user logged in First Time.
     *
     * @param id
     * @param input
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("{id}")
    public Response updateFirstLoginPassword(@PathParam("id") int id, InputStream input) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            User.changeFirstLoginPassword(id,node);
            resp = Response.ok("{\"Message\":" + "\" Password changed Successfully. \"}").build();
        }catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Add User
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param firstname
     * @param lastname
     * @param username
     * @param isActive
     * @param addLine1
     * @param addLine2
     * @param addLine3
     * @param city
     * @param state
     * @param phones
     * @param designation
     * @param empNumber
     * @param roleid
     * @param divId
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addUser(
            @FormDataParam("profileImage") InputStream fileInputStream,
            @FormDataParam("profileImage") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("firstName") String firstname,
            @FormDataParam("lastName") String lastname,
            @FormDataParam("username") String username,
            @FormDataParam("isActive") boolean isActive,
            @FormDataParam("addLine1") String addLine1,
            @FormDataParam("addLine2") String addLine2,
            @FormDataParam("addLine3") String addLine3,
            @FormDataParam("city") String city,
            @FormDataParam("state") String state,
            @FormDataParam("phones") String phones,
            @FormDataParam("designation") String designation,
            @FormDataParam("empNumber") String empNumber,
            @FormDataParam("roleid") int roleid,
            @FormDataParam("divId") String divId,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Multipart Form Data");

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);


            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store image in AWS bucket.
                uploadFilePath = User.writeToFile(fileInputStream, fileName);
            } else {
//                uploadFilePath = "https://s3.amazonaws.com/com.brewconsulting.client1/Profile/1479199419218_default_image.jpg";
                uploadFilePath = "";
            }

            int userId = User.addUser(firstname,lastname,username,isActive,addLine1,addLine2,addLine3,city,state,phones,
                    designation,empNumber,uploadFilePath,roleid,divId,(LoggedInUser) crc.getProperty("userObject"));

            if (userId != 0)
                resp = Response.ok("{\"id\":" + userId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert User")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add User\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (BadRequestException b) {
            logger.error("BadRequestException " ,b);
            resp = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"Message\":" + "\"" + b.getMessage()  +"\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
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
     *  Update User Details
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param firstname
     * @param lastname
     * @param username
     * @param isActive
     * @param addLine1
     * @param addLine2
     * @param addLine3
     * @param city
     * @param state
     * @param phones
     * @param designation
     * @param empNumber
     * @param roleid
     * @param divId
     * @param isUpdated
     * @param url
     * @param userid
     * @param isPublic
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateUser(
            @FormDataParam("profileImage") InputStream fileInputStream,
            @FormDataParam("profileImage") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("firstName") String firstname,
            @FormDataParam("lastName") String lastname,
            @FormDataParam("username") String username,
            @FormDataParam("isActive") boolean isActive,
            @FormDataParam("addLine1") String addLine1,
            @FormDataParam("addLine2") String addLine2,
            @FormDataParam("addLine3") String addLine3,
            @FormDataParam("city") String city,
            @FormDataParam("state") String state,
            @FormDataParam("phones") String phones,
            @FormDataParam("designation") String designation,
            @FormDataParam("empNumber") String empNumber,
            @FormDataParam("roleid") int roleid,
            @FormDataParam("divId") String divId,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("userid") int userid,
            @FormDataParam("isPublic") boolean isPublic,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if(isUpdated)
            {
                if (fileFormDataContentDisposition != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = User.writeToFile(fileInputStream, fileName);
                }
            }
            else
            {
                uploadFilePath = url;
            }

            int affectedRows = User.updateUserDetails(firstname,lastname,username,isActive,addLine1,addLine2,addLine3,city,state,phones,
                    designation,empNumber,uploadFilePath,roleid,divId,userid,isPublic,(LoggedInUser) crc.getProperty("userObject"));

            if (affectedRows != 0 || affectedRows >0)
                resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Update User Details")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update User Details\"}")
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
     *  Used to update user profile details.
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param firstname
     * @param lastname
     * @param addLine1
     * @param addLine2
     * @param addLine3
     * @param city
     * @param state
     * @param phones
     * @param isUpdated
     * @param url
     * @param userid
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("profile")
    public Response updateUserProfile(
            @FormDataParam("profileImage") InputStream fileInputStream,
            @FormDataParam("profileImage") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("firstName") String firstname,
            @FormDataParam("lastName") String lastname,
            @FormDataParam("addLine1") String addLine1,
            @FormDataParam("addLine2") String addLine2,
            @FormDataParam("addLine3") String addLine3,
            @FormDataParam("city") String city,
            @FormDataParam("state") String state,
            @FormDataParam("phones") String phones,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("userid") int userid,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if(isUpdated)
            {
                if (fileFormDataContentDisposition != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = User.writeToFile(fileInputStream, fileName);
                }
            }
            else
            {
                uploadFilePath = url;
            }

            int affectedRows = User.updateUserProfileDetails(firstname,lastname,addLine1,addLine2,addLine3,city,state,phones,
                   uploadFilePath,userid,(LoggedInUser) crc.getProperty("userObject"));

            if (affectedRows != 0 || affectedRows >0)
                resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Update User Details")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update User Details\"}")
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
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Change password of User When user logged in First Time.
     *
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("logout")
    public Response removeDeviceDetails(@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = User.removeDeviceDetails((LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRow\":" + affectedRow + "}").build();

        }catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Update device details
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("devicedetails")
    public Response updateDeviceDetails(InputStream input,@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int affectedRow = User.updateDeviceDetails(node,(LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok("{\"affectedRow\":" + affectedRow + "}").build();

        }catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}