package com.brewconsulting.login;

import java.security.MessageDigest;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.*;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.sql.*;

import com.brewconsulting.DB.masters.User;
import com.brewconsulting.DB.masters.UserViews;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.brewconsulting.DB.masters.ForgotPassword.generateAndSendEmail;

@Path("login")
public class authentication {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(Credentials credentials, @Context ServletContext context)
            throws SQLException, ClassNotFoundException, IOException {
        Response resp = null;
        try {
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

            System.out.println("Digest(in hex format):: " + sb.toString());

            if (username.equals(null) || username.equals("")) {
                resp = Response.status(Response.Status.BAD_REQUEST).entity("Username not specified").build();
                throw new Exception("Username not specified");
            }
            if (password.equals(null) || password.equals("")) {
                resp = Response.status(Response.Status.BAD_REQUEST).entity("Password not specified").build();
                throw new Exception("Password not specified");
            }

            User user = User.authenticate(username, sb.toString());

            if (user == null) {
                resp = Response.status(Response.Status.UNAUTHORIZED).entity("Authentication Failed").build();
                throw new Exception("User authentication failed.");

            }
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();

            if (context == null) {
                resp = Response.serverError().entity("ServeletContext missing").build();
                throw new Exception("Servlet context missing");
            }

            String salt = context.getInitParameter("salt");
            if (salt == null) {
                resp = Response.serverError().entity("Salt value missing").build();
                throw new Exception("SALT value missing");
            }

            JwtBuilder bldr = Jwts.builder().setIssuedAt(new Date()).setIssuer("brewconsulting.com")
                    .setSubject(user.username).setId(UUID.randomUUID().toString())
                    .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(3600)))
                    .signWith(SignatureAlgorithm.HS256, salt);

            // Add users details to claims. This will prevent a DB roundtrip for
            // each API call.

            bldr.claim("user", mapper.writerWithView(UserViews.authView.class).writeValueAsString(user));

            node.put("jwt", bldr.compact());

            resp = Response.ok(node.toString()).build();
        } catch (Exception ex) {
            if (resp == null)
                resp = Response.serverError().entity(ex.getMessage()).build();
            ex.printStackTrace();
        }
        return resp;
    }



}
