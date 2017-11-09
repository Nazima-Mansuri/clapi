package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.common.DBConnectionProvider;

import javax.jws.soap.SOAPBinding;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

/**
 * Created by lcom53 on 12/10/16.
 */

/***
 * Forgot Password Class.
 * In which mail will send to user with Auto generated Alpha-numeric characters.
 * And that new password updated in Database.
 */
public class ForgotPassword {

    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;

    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 8;

    /**
     * This method generates random string
     *
     * @return
     */
    public static String generateRandomString() {

        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    /**
     * This method generates random numbers
     *
     * @return int
     */
    private static int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    /***
     * Method used to Update password in database.
     *
     * @param username
     * @param password
     * @return
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static int updatePassword(String username, String password) throws SQLException, NamingException, ClassNotFoundException {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        try {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            System.out.println("Digest(in hex format):: " + sb.toString());

            stmt = con.prepareStatement("UPDATE master.users SET password = ? where username = ? ");
            stmt.setString(1, sb.toString());
            stmt.setString(2, username);
            int affectedRows = stmt.executeUpdate();
            return affectedRows;
        } finally {
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }

    }

    /***
     * Method is used to get User's details
     *
     * @param username
     * @return
     * @throws Exception
     */
    public static String getUserDetail(String username) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result;
        User user = null;
        String firstName = null, lastName = null;
        try {
            stmt = con.prepareStatement("SELECT username from master.users where username = ?");
            stmt.setString(1, username);
            result = stmt.executeQuery();
            if (result.next()) {
                stmt = con.prepareStatement("SELECT firstname , lastname from master.users where username = ?");
                stmt.setString(1, username);
                result = stmt.executeQuery();

                if (result.next()) {
                    firstName = result.getString(1);
                    lastName = result.getString(2);
                }
            } else {
                throw new SQLException("User does not exist");
            }

        } finally {
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }
        return firstName + " " + lastName;
    }

    /***
     * Thios method used to send Email with random generated alphanumeric characters
     *
     * @param username
     * @param from
     * @param password
     * @return
     * @throws MessagingException
     */
    public static boolean generateAndSendEmail(String username, String from, String password) throws Exception {

        // Step1
        /*mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");*/

        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        mailServerProperties.put("mail.smtp.host", "smtp.gmail.com");
        mailServerProperties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(mailServerProperties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        // Step2
        String name = getUserDetail(username);
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(session);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
        generateMailMessage.setSubject("Rolla > Forgot Password");
        String forgotPassword = generateRandomString();
        String emailBody = "<h3> Hi " + name + ", </h3>" + " <h4> We got your request for new password. </h4>" + "<h4>Your New Password : " + forgotPassword + "</h4>";
        System.out.println(forgotPassword);
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");

        // Step3
        Transport transport = getMailSession.getTransport("smtp");

        transport.connect("smtp.gmail.com", from, password);
        if (transport.isConnected()) {
            try {
                transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            System.out.println(forgotPassword);
            updatePassword(username, forgotPassword);
            transport.close();
            return true;
        } else {
            return false;
        }
    }

    public static boolean SendEmail(String username, String from, String password) throws Exception {

        boolean isSuccess = false;
        String name = getUserDetail(username);
        String forgotPassword = generateRandomString();

        final String BODY = "<h3> Hi " + name + ", </h3>" + " <h4> We got your request for new password. </h4>" + "<h4>Your New Password : " + forgotPassword + "</h4>";
        final String SUBJECT = "Rolla > Forgot Password";

        // Supply your SMTP credentials below. Note that your SMTP credentials are different from your AWS credentials.
        final String SMTP_USERNAME = "AKIAIKDGY5ML2NZ4RD6A";  // Replace with your SMTP username.
        final String SMTP_PASSWORD = "ArNrLJ6VL4RVqPXPw4JggSQt6QJ10LzdnidYPH8qFgYl";  // Replace with your SMTP password.

        // Amazon SES SMTP host name. This example uses the US West (Oregon) Region.
        final String HOST = "email-smtp.us-east-1.amazonaws.com";

        // The port you will connect to on the Amazon SES SMTP endpoint. We are choosing port 25 because we will use
        // STARTTLS to encrypt the connection.
        final int PORT = 25;

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtp.port", PORT);

        // Set properties indicating that we want to use STARTTLS to encrypt the connection.
        // The SMTP session will begin on an unencrypted connection, and then the client
        // will issue a STARTTLS command to upgrade to an encrypted connection.
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(username));
        msg.setSubject(SUBJECT);
        msg.setContent(BODY, "text/plain");

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try {
            System.out.println("Attempting to send an email through the Amazon SES SMTP interface...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
//            transport.sendMessage(msg, msg.getAllRecipients());

            if (transport.isConnected()) {
                transport.sendMessage(msg, msg.getAllRecipients());
                System.out.println(forgotPassword);
                updatePassword(username, forgotPassword);
                transport.close();
                isSuccess = true;
            } else {
                isSuccess = false;
            }

            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            // Close and terminate the connection.
            transport.close();
        }
        return isSuccess;

     /*   // Step1
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
        String name = getUserDetail(username);
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
        generateMailMessage.setSubject("Rolla > Forgot Password");
        String forgotPassword = generateRandomString();
        String emailBody = "<h3> Hi " + name + ", </h3>" + " <h4> We got your request for new password. </h4>" + "<h4>Your New Password : " + forgotPassword + "</h4>";
        System.out.println(forgotPassword);
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");

        // Step3
        Transport transport = getMailSession.getTransport("smtp");

        transport.connect("smtp.gmail.com", from, password);
        if (transport.isConnected()) {
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            System.out.println(forgotPassword);
            updatePassword(username, forgotPassword);
            transport.close();
            return true;
        } else {
            return false;
        }*/
    }
}