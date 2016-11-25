package com.brewconsulting.users;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.masters.*;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Credentials;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.postgresql.util.PSQLException;

import com.brewconsulting.exceptions.RequiredDataMissing;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("users")
@Secured
public class Users {

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
            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User.getProfile(
                            (LoggedInUser) crc.getProperty("userObject"), id))).build();
        }
        catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to view other's profile\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }catch (Exception e) {
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
            resp = Response.ok(
                    mapper.writerWithView(UserViews.deAssociateView.class).writeValueAsString(User
                            .getDeassociateUser(divId,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Deassociate User\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
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
            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllUsers((LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }


    /***
     * Produces list of users for specific Division.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("userbydivision/{id}")
    public Response getAllUsersByDivId(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resp = Response.ok(
                    mapper.writerWithView(UserViews.profileView.class).writeValueAsString(User
                            .getAllUsersByDivId(id, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Users. \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Produces al Roles
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
            resp = Response.ok(
                    mapper.writeValueAsString(Role.getAllRoles(
                            (LoggedInUser) crc.getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Roles\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
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
            resp = Response.ok(
                    mapper.writerWithView(UserViews.clientView.class).writeValueAsString(User
                            .getAllClients((LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Clients. \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
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
            JsonNode node = mapper.readTree(input);
            int affectedRow = User.deactivateUser(node, (LoggedInUser) crc.getProperty("userObject"));
            System.out.println("Method called and affected rows" + affectedRow);
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }  catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Deactivate User \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Change password of User
     * @param id
     * @param input
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    @Path("{id}")
    public Response updatePassword(@PathParam("id") int id, InputStream input) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(input);
            User.changePassword(id,node);
            resp = Response.ok("{\"Message\":" + "\" Password changed Successfully. \"}").build();
        }catch (IOException e) {
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            e.printStackTrace();
        } catch (Exception e) {
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
     * @param clientId
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
            @FormDataParam("clientId") int clientId,
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
            @FormDataParam("divId") int divId,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Multipart Form Data");

        try {

            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store image in AWS bucket.
                uploadFilePath = User.writeToFile(fileInputStream, fileName);
            } else {
                uploadFilePath = "https://s3.amazonaws.com/com.brewconsulting.client1/Profile/1479199419218_default_image.jpg";
            }

            int userId = User.addUser(firstname,lastname,username,clientId,isActive,addLine1,addLine2,addLine3,city,state,phones,
                    designation,empNumber,uploadFilePath,roleid,divId,(LoggedInUser) crc.getProperty("userObject"));

            if (userId != 0)
                resp = Response.ok("{\"id\":" + userId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert User")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add User\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (BadRequestException b) {
            resp = Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"Message\":" + "\"" + b.getMessage()  +"\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
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
     * @param clientId
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
            @FormDataParam("clientId") int clientId,
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
            @FormDataParam("divId") int divId,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("userid") int userid,
            @FormDataParam("isPublic") boolean isPublic,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";

        System.out.println("Multipart Form Data");

        try {
            if(isUpdated)
            {
                System.out.println("isUpdated : " + isUpdated);
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
                System.out.println("isUpdated : " + isUpdated);
            }

            int affectedRows = User.updateUserDetails(firstname,lastname,username,clientId,isActive,addLine1,addLine2,addLine3,city,state,phones,
                    designation,empNumber,uploadFilePath,roleid,divId,userid,isPublic,(LoggedInUser) crc.getProperty("userObject"));

            if (affectedRows != 0 || affectedRows >0)
                resp = Response.ok("{\"affectedRows\":" + affectedRows + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Update User Details")
                                .getJsonString()).build();

        }   catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to Update User Details\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}