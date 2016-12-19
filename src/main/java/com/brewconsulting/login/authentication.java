package com.brewconsulting.login;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.*;

import javax.mail.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.DB.masters.ForgotPassword;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.masters.Mem;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.*;

import java.io.IOException;
import java.sql.*;

import com.brewconsulting.DB.masters.User;
import com.brewconsulting.DB.masters.UserViews;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import static com.brewconsulting.DB.masters.ForgotPassword.generateAndSendEmail;

@Path("login")
public class authentication {

    static final Logger logger = Logger.getLogger(authentication.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    @POST
    public Response login(Credentials credentials, @Context ServletContext context)
            throws SQLException, ClassNotFoundException, IOException {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            String username = credentials.getUsername();
            String password = credentials.getPassword();

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            if (username.equals(null) || username.equals("")) {
                resp = Response.status(Response.Status.BAD_REQUEST).entity("{\"Message\":" + "\" Username not specified\"}").build();
                throw new Exception("Username not specified");
            }
            if (password.equals(null) || password.equals("")) {
                resp = Response.status(Response.Status.BAD_REQUEST).entity("{\"Message\":" + "\" Password not specified\"}").build();
                throw new Exception("Password not specified");
            }

            User user = User.authenticate(username, sb.toString());

            if (user == null) {

                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\" Authentication Failed \"}")
                        .type(MediaType.APPLICATION_JSON).build();
                throw new Exception("User authentication failed.");

            }
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();

            if (context == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\" ServeletContext missing\"}").build();
                throw new Exception("Servlet context missing");
            }

            String salt = context.getInitParameter("salt");
            if (salt == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\" Salt value missing \"}").build();
                throw new Exception("SALT value missing");
            }

            javax.naming.Context env = null;
            env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
            int accessTimeout = 0;
            int refreshTimeout = 0;
            if (credentials.getIsPublic()) {

                accessTimeout = (int) env.lookup("ACCESS_TOKEN_PUBLIC_TIMEOUT");
                refreshTimeout = (int) env.lookup("REFRESH_TOKEN_PUBLIC_TIMEOUT");
            } else {
                accessTimeout = (int) env.lookup("ACCESS_TOKEN_WORK_TIMEOUT");
                refreshTimeout = (int) env.lookup("REFRESH_TOKEN_WORK_TIMEOUT");
            }

            if (accessTimeout < 1)
                throw new Exception("Access token timeout not specified.");

            if (refreshTimeout < 1)
                throw new Exception("Refresh token timeout not specified");

            // access token that expires in a small time.
            JwtBuilder bldr = Jwts.builder().setIssuedAt(new Date()).setIssuer("brewconsulting.com")
                    .setSubject(user.username).setId(UUID.randomUUID().toString())
                    .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(accessTimeout)))
                    .signWith(SignatureAlgorithm.HS256, salt);

            // Add users details to claims. This will prevent a DB roundtrip for
            // each API call.

            bldr.claim("user", mapper.writerWithView(UserViews.authView.class).writeValueAsString(user));
            bldr.claim("tokenType", "ACCESS");

            node.put("accessToken", bldr.compact());

            // refresh token that expires in long time
            bldr = Jwts.builder().setIssuedAt(new Date()).setIssuer("brewconsulting.com").setSubject(user.username)
                    .setId(UUID.randomUUID().toString())
                    .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(refreshTimeout)))
                    .signWith(SignatureAlgorithm.HS256, salt);

            bldr.claim("user", mapper.writerWithView(UserViews.authView.class).writeValueAsString(user));
            bldr.claim("tokenType", "REFRESH");
            node.put("refreshToken", bldr.compact());

            node.put("isFirstLogin",user.isFirstLogin);
            node.put("userId",user.id);
            node.put("roleId",user.roles.get(0).roleId);
            node.put("roleName",user.roles.get(0).roleName);
            node.put("fullName",user.firstName + " " + user.lastName);
            node.put("profileImage",user.profileImage);

            resp = Response.ok("" + node.toString() + "").type(MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) {
            logger.error("Exception " ,ex);
            if (resp == null)
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\"" + ex.getMessage()  +"\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        }
        return resp;
    }

    /**
     * 1. Check in memcached if the user is blocked. If yes then say 401. The client will then be redirected to
     * login page.
     * 2. if not blocked in memcached then take the refresh token and check in DB if the user is active
     * 3. If user active fetch all the fields that go into access token like name, roles, divisions etc.
     * 4. Create access token and give client. The client uses this token for all future calls.
     *
     * @param credentials
     * @param context
     * @return
     */

    @Path("refresh")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshAccessToken(Credentials credentials, @Context ContainerRequestContext context, @Context ServletContext servletContext) throws SQLException, NamingException, ClassNotFoundException, IOException {
        Response resp = null;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        String salt = servletContext.getInitParameter("salt");

        properties.load(inp);
        PropertyConfigurator.configure(properties);

        String refreshToken = credentials.getRefreshToken();

            if(refreshToken == null || refreshToken == "")
            {
                context.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
            else
            {
                try {
                    boolean isExist = Mem.getToken("refreshToken",refreshToken);
                    if(!isExist) {
                        Jws<Claims> clms = Jwts.parser().setSigningKey(salt).parseClaimsJws(refreshToken);
                        JsonNode jsonNode = mapper.readTree((String) clms.getBody().get("user"));
                        String tokenType = (String) clms.getBody().get("tokenType");
                        context.setProperty("userObject", mapper.treeToValue(jsonNode, LoggedInUser.class));

                        User user = User.getNewAccessToken((LoggedInUser) context.getProperty("userObject"));

                        javax.naming.Context env = null;
                        env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
                        int accessTimeout = 0;
                        if (salt == null) {
                            resp = Response.serverError().entity("{\"Message\":" + "\" Salt value missing \"}").build();
                            throw new Exception("SALT value missing");
                        }


                        if (credentials.getIsPublic()) {

                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_PUBLIC_TIMEOUT");

                        } else {
                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_WORK_TIMEOUT");

                        }

                        if (accessTimeout < 1)
                            throw new Exception("Access token timeout not specified.");

                        if (user != null) {
                            // access token that expires in short time.
                            JwtBuilder bldr = Jwts.builder().setIssuedAt(new Date()).setIssuer("brewconsulting.com")
                                    .setSubject(user.username).setId(UUID.randomUUID().toString())
                                    .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(accessTimeout)))
                                    .signWith(SignatureAlgorithm.HS256, salt);

                            bldr.claim("user", mapper.writerWithView(UserViews.authView.class).writeValueAsString(user));
                            bldr.claim("tokenType", "ACCESS");

                            node.put("accessToken", bldr.compact());

                            Mem.deleteData(user.id + "#DEACTIVATED");
                            Mem.deleteData(user.username + "#ROLECHANGED");
                            resp = Response.ok(node.toString()).build();

                        } else {
                            resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\" You are not authorized,Please Login again.\"}")
                                    .type(MediaType.APPLICATION_JSON).build();
                            throw new NotAuthorizedException("You are not authorized,Please Login again.");

                        }
                    }
                    else
                    {
                        resp = Response.status(498).entity("{\"Message\":" + "\" Invalid Token , Please Login Again!\"}").build();
                    }
                }
                catch (NotAuthorizedException na)
                {
                    logger.error("NotAuthorizedException ",na);
                    resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\" "+ na.toString() +" \" }")
                            .type(MediaType.APPLICATION_JSON).build();
                }
                catch (Exception ex) {
                    logger.error("Exception ",ex);
                    context.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build());
                    servletContext.log("Invalid token", ex);
                    resp = Response.status(Response.Status.UNAUTHORIZED).entity("{\"Message\":" + "\" "+ ex.toString() +" \" }")
                            .type(MediaType.APPLICATION_JSON).build();
                }
            }
        return resp;
    }

    /***
     * Forgot Password
     *
     * @param credentials
     * @param context
     * @return
     */
    @POST
    @Path("/forgotpassword")
    @Produces("application/json")
    public Response forgotPassword(Credentials credentials, @Context ServletContext context)
    {
        Response resp = null;

        String from = context.getInitParameter("from");
        String password = context.getInitParameter("password");

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            boolean isTrue = ForgotPassword.generateAndSendEmail(credentials.getUsername(),from,password);
            System.out.println("isTrue " + isTrue);
            if(isTrue)
            {
                resp = Response.ok().entity("{\"Message\":" + "\" Password send by Email Succesfully.\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            }
            else
            {
                resp = Response.serverError().entity("{\"Message\":" + "\" Something went wrong\"}")
                        .type(MediaType.APPLICATION_JSON).build();
            }
        }
        catch (Exception e)
        {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }
}
