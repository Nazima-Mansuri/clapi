
package com.brewconsulting.DB.masters;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.masters.Mem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.simple.JSONObject;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class User {

    @JsonProperty("clientId")
    @JsonView({UserViews.authView.class, UserViews.clientView.class})
    public int clientId; // -1 means no client

    @JsonProperty("id")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class})
    public int id;

    @JsonProperty("username")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class})
    public String username;

    @JsonView({UserViews.authView.class, UserViews.clientView.class})
    @JsonProperty("schemaName")
    public String schemaName;

    @JsonView({UserViews.authView.class, UserViews.deAssociateView.class})
    @JsonProperty("firstName")
    public String firstName;

    @JsonView({UserViews.authView.class, UserViews.deAssociateView.class})
    @JsonProperty("lastName")
    public String lastName;

    @JsonView(UserViews.authView.class)
    @JsonProperty("roles")
    public ArrayList<Role> roles;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("addLine1")
    public String addLine1;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("addLine2")
    public String addLine2;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("addLine3")
    public String addLine3;

    @JsonView({UserViews.profileView.class, UserViews.deAssociateView.class})
    @JsonProperty("city")
    public String city;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("state")
    public String state;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("roleid")
    public int roleid;

    @JsonView({UserViews.profileView.class, UserViews.divView.class})
    @JsonProperty("divId")
    public int divId;

    @JsonView({UserViews.profileView.class, UserViews.divView.class})
    @JsonProperty("divName")
    public String divName;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("empNumber")
    public String empNum;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("phones")
    public String[] phones;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("createBy")
    public int createBy;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("updateDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateDate;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("updateBy")
    public int updateBy;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("profileImage")
    public String profileImage;

    @JsonView({UserViews.profileView.class, UserViews.clientView.class})
    @JsonProperty("isActive")
    public boolean isActive;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("designation")
    public String designation;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonView({UserViews.profileView.class, UserViews.clientView.class})
    @JsonProperty("clientName")
    public String clientName;

    @JsonView(UserViews.authView.class)
    @JsonProperty("isFirstLogin")
    public boolean isFirstLogin;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("divisionName")
    public ArrayList<String> divisionName;

    // make the constructor private.
    protected User() {

    }

    public static final int User = 1;

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
     * Method used to change password When user get login first time .
     *
     * @param id
     * @param node
     * @return
     * @throws Exception
     */
    public static int changeFirstLoginPassword(int id, JsonNode node) throws Exception {


        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int affectedRows;

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(node.get("password").asText().getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Digest(in hex format):: " + sb.toString());

        try {
            stmt = con.prepareStatement("Update master.users set password = ? where id = ?");
            stmt.setString(1, sb.toString());
            stmt.setInt(2, id);
            affectedRows = stmt.executeUpdate();

            stmt = con.prepareStatement("Update master.users set isfirstlogin = ? where id = ?");
            stmt.setBoolean(1, false);
            stmt.setInt(2, id);
            affectedRows = stmt.executeUpdate();
        } finally {
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }
        return affectedRows;
    }

    /***
     * Method is used to change password.
     *
     * @param node
     * @return
     * @throws Exception
     */
    public static int changePassword(JsonNode node) throws Exception {

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int affectedRows = 0;
        ResultSet resultSet = null;
        String password = null, oldPassword, newPassword;

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(node.get("oldPassword").asText().getBytes());

        byte oldByteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < oldByteData.length; i++) {
            sb.append(Integer.toString((oldByteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        oldPassword = sb.toString();

        System.out.println("OLD Digest(in hex format):: " + sb.toString());

        md.update(node.get("newPassword").asText().getBytes());

        byte newByteData[] = md.digest();

        //convert the byte to hex format method 1
        sb = new StringBuffer();
        for (int i = 0; i < newByteData.length; i++) {
            sb.append(Integer.toString((newByteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        newPassword = sb.toString();

        System.out.println("NEW Digest(in hex format):: " + sb.toString());

        try {

            stmt = con.prepareStatement(" SELECT password FROM master.users WHERE id = ? ");
            stmt.setInt(1, node.get("userId").asInt());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                password = resultSet.getString(1);
                System.out.println("Password : " + password);
            }

            if (password.equals(oldPassword)) {

                stmt = con.prepareStatement("Update master.users set password = ? where id = ?");
                stmt.setString(1, newPassword);
                stmt.setInt(2, node.get("userId").asInt());
                affectedRows = stmt.executeUpdate();
            } else
                throw new InputMismatchException("");
        } finally {
            if (resultSet != null)
                if (!resultSet.isClosed())
                    resultSet.close();
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }
        return affectedRows;
    }

    /***
     * This method checks User is active or not and user's roles are changes or not.
     *
     * @param loggedInUser
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NamingException
     */
    public static User getNewAccessToken(LoggedInUser loggedInUser) throws ClassNotFoundException, SQLException, NamingException {

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        boolean isActive;
        User user = null;

        try {

            stmt = con.prepareStatement(
                    "select a.isActive"
                            + " from master.users a where a.id=?");
            stmt.setInt(1, loggedInUser.id);
            ResultSet resultSet = stmt.executeQuery();

//            boolean data = Mem.getData(loggedInUser.id + "#DEACTIVATED");

                if (resultSet.next()) {
                    isActive = resultSet.getBoolean(1);

                    if (isActive) {
                        stmt = con.prepareStatement(
                                "select a.id, a.clientId, a.firstName, a.lastName, schemaName, d.id roleid, d.name rolename, a.username "
                                        + " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
                                        + " where a.isActive  and a.clientId = b.id and "
                                        + " a.id = c.userId and c.roleId = d.id and a.id = ?");

                        stmt.setInt(1, loggedInUser.id);

                        final ResultSet masterUsers = stmt.executeQuery();
                        while (masterUsers.next()) {
                            if (user == null) { // execute for the first iteration
                                if(loggedInUser.roles.get(0).roleId != masterUsers.getInt(6) &&
                                        !loggedInUser.roles.get(0).roleName.equals(masterUsers.getString(7)))
                                {
                                    throw new NotAuthorizedException("");
                                }

                                user = new User();
                                user.id = masterUsers.getInt(1);
                                user.clientId = masterUsers.getInt(2);
                                user.firstName = masterUsers.getString(3);
                                user.lastName = masterUsers.getString(4);
                                user.schemaName = masterUsers.getString(5);
                                user.username = masterUsers.getString(8);
                                user.roles = new ArrayList<Role>();
                                user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));
                                continue;
                            }

                            if (user.roles != null) {
                                user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));
                            }
                        }
                    } else {
                        return null;
                    }
                }
        }  finally {
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }
        return user;
    }

    /***
     * If username and password exists , return details of that User.
     *
     * @param username
     * @param password
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NamingException
     */
    public static User authenticate(String username, String password,String deviceToken,String deviceOS)
            throws ClassNotFoundException, SQLException, NamingException {
        User user = null;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(
                    "select a.id, a.clientId, a.firstName, a.lastName, schemaName, d.id roleid, d.name rolename, a.username ,a.isfirstlogin "
                            + " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
                            + " where a.isActive and a.username = ? and a.password = ? and a.clientId = b.id and "
                            + " a.id = c.userId and c.roleId = d.id");
            stmt.setString(1, username);
            stmt.setString(2, password);

            final ResultSet masterUsers = stmt.executeQuery();
            while (masterUsers.next()) {
                if (user == null) { // execute for the first iteration
                    user = new User();
                    user.id = masterUsers.getInt(1);
                    user.clientId = masterUsers.getInt(2);
                    user.firstName = masterUsers.getString(3);
                    user.lastName = masterUsers.getString(4);
                    user.schemaName = masterUsers.getString(5);
                    user.username = masterUsers.getString(8);
                    user.isFirstLogin = masterUsers.getBoolean(9);

                    stmt = con.prepareStatement(" SELECT profileimage from " + masterUsers.getString(5) + ".userprofile where userid = ? ");
                    stmt.setInt(1, masterUsers.getInt(1));
                    final ResultSet result = stmt.executeQuery();
                    while (result.next()) {
                        user.profileImage = result.getString(1);
                    }

                    user.roles = new ArrayList<Role>();
                    user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));
                    continue;
                }

                if (user.roles != null) {
                    user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));
                }
            }

            if((!deviceToken.isEmpty() && !deviceOS.isEmpty())) {
                stmt = con.prepareStatement(" UPDATE master.users SET devicetoken = ? , deviceos = ? " +
                        " WHERE id = ? AND username = ? AND password = ? ");
                stmt.setString(1, deviceToken);
                stmt.setString(2, deviceOS);
                stmt.setInt(3, user.id);
                stmt.setString(4, username);
                stmt.setString(5, password);

                stmt.executeUpdate();
            }

        } finally {
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }

        return user;
    }

    /***
     * Get the basic details of the user.
     *
     * @param id
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NamingException
     */
    public static User find(Integer id) throws ClassNotFoundException, SQLException, NamingException {
        Connection con = DBConnectionProvider.getConn();
        User user = null;
        try {

            PreparedStatement stmt = con.prepareStatement(
                    "select a.id, a.clientId, a.firstname, a.lastname, schemaName, d.id roleid, d.name rolename, a.username "
                            + " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
                            + " where a.isActive and a.id = ? and a.clientId = b.id and "
                            + " a.id = c.userId and c.roleId = d.id");
            stmt.setInt(1, id);
            final ResultSet masterUsers = stmt.executeQuery();
            while (masterUsers.next()) {
                if (user == null) { // execute for the first iteration
                    user = new User();
                    user.id = id;
                    user.clientId = masterUsers.getInt(2);
                    user.schemaName = masterUsers.getString(5);
                    user.firstName = masterUsers.getString(3);
                    user.lastName = masterUsers.getString(4);
                    user.username = masterUsers.getString(8);
                    user.roles = new ArrayList<Role>();
                    user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));

                    continue;
                }

                if (user.roles != null) {
                    user.roles.add(new Role(masterUsers.getInt(6), masterUsers.getString(7)));
                }
            }
        } finally {
            if (con != null)
                con.close();
        }
        return user;

    }

    /***
     * User profile class inherits User and includes profile data that comes
     * from client db schema.
     *
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static User getProfile(LoggedInUser loggedInUser, int id) throws Exception {
        // check if the profile request of for self? else check if the role
        // allows it.
        User user = null;

        // check if the user has permission to see others profile.
        if (loggedInUser.id != id) {

            int userRole = loggedInUser.roles.get(0).roleId;

            if (Permissions.isAuthorised(userRole, User).equals("Read") ||
                    Permissions.isAuthorised(userRole, User).equals("Write")) {
                user = fillFullProfile(loggedInUser, id);
            } else
                throw new NotAuthorizedException("");

            // user = new UserProfile(find(id));

        } else {
            user = new User();
            user.id = loggedInUser.id;
            user.clientId = loggedInUser.clientId;
            user.schemaName = loggedInUser.schemaName;
            user.firstName = loggedInUser.firstName;
            user.lastName = loggedInUser.lastName;
            user.username = loggedInUser.username;
            user.roles = new ArrayList<Role>();
            user.roles.add(new Role(loggedInUser.roles.get(0).roleId, loggedInUser.roles.get(0).roleName));
            fillProfileInfo(user);
        } // else

        // admin users will not have their profiles filled with any other
        // details as they are not in
        // any firm DB.
        // if (!isUserAdmin(user))
        // fillProfileInfo(user);

        return user;
    }

    /**
     * Give user's full Profile Information
     *
     * @param user
     * @param id
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NamingException
     */
    private static User fillFullProfile(LoggedInUser user, int id)
            throws Exception {
        Connection con = DBConnectionProvider.getConn();
        ResultSet schemaUsers = null;
        PreparedStatement stmt = null;
        User userProfile = null;
        int userRole = user.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, User).equals("Read") ||
                Permissions.isAuthorised(userRole, User).equals("Write")) {

            try {
                stmt = con.prepareStatement(
                        "select a.id id, a.clientId clientid,h.name clientname , a.firstname firstname, a.lastname lastname, h.schemaName schemaname,"
                                + "c.id roleid, c.name rolename, a.username username, "
                                + "(d.address).addLine1 line1, (d.address).addLine2 line2, (d.address).addLine3 line3,"
                                + "(d.address).city city, (d.address).state, (d.address).phone phones, d.designation, g.divId divId,"
                                + "empNumber, e.name divname," + "d.createDate cdate, d.createBy cby, "
                                + "d.updateDate  udate,  d.updateBy uby, d.profileimage profileimage"
                                + " from master.users a, master.userRoleMap b, master.roles c, " + user.schemaName
                                + ".userProfile d," + user.schemaName + ".divisions e, master.clients f," + user.schemaName + ".userdivmap g , master.clients h"
                                + " where a.isActive and a.id = ? and a.id = b.userId and b.roleId = c.id"
                                + " and d.userId = a.id and g.divId = e.Id and f.id = a.clientId and h.id = a.clientId");
                stmt.setInt(1, id);
                schemaUsers = stmt.executeQuery();
                while (schemaUsers.next()) {
                    if (userProfile == null) {
                        userProfile = new User();
                        userProfile.id = schemaUsers.getInt("id");
                        userProfile.clientId = schemaUsers.getInt("clientid");
                        userProfile.clientName = schemaUsers.getString("clientname");
                        userProfile.firstName = schemaUsers.getString("firstname");
                        userProfile.lastName = schemaUsers.getString("lastname");
                        userProfile.schemaName = schemaUsers.getString("schemaname");
                        userProfile.roles = new ArrayList<Role>();
                        userProfile.roles.add(new Role(schemaUsers.getInt("roleid"), schemaUsers.getString("rolename")));
                        userProfile.username = schemaUsers.getString("username");
                        userProfile.addLine1 = schemaUsers.getString("line1");
                        userProfile.addLine2 = schemaUsers.getString("line2");
                        userProfile.addLine3 = schemaUsers.getString("line3");
                        userProfile.city = schemaUsers.getString("city");
                        userProfile.state = schemaUsers.getString("state");
                        userProfile.phones = (String[]) schemaUsers.getArray("phones").getArray();
                        userProfile.roleid = schemaUsers.getInt("roleid");
                        userProfile.divId = schemaUsers.getInt("divId");
                        userProfile.divName = schemaUsers.getString("divname");
                        userProfile.empNum = schemaUsers.getString("empnumber");
                        userProfile.createDate = schemaUsers.getDate("cdate");
                        userProfile.createBy = schemaUsers.getInt("cby");
                        userProfile.updateDate = schemaUsers.getDate("cdate");
                        userProfile.updateBy = schemaUsers.getInt("cby");
                        userProfile.profileImage = schemaUsers.getString("profileimage");
                        userProfile.designation = schemaUsers.getString("designation");
//                        continue;
                    }
//                    userProfile.roles.add(new Role(schemaUsers.getInt("roleid"), schemaUsers.getString("rolename")));

                }

                return userProfile;

            } finally {
                if (schemaUsers != null)
                    if (!schemaUsers.isClosed())
                        schemaUsers.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param user
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws NamingException
     */
    private static void fillProfileInfo(User user) throws ClassNotFoundException, SQLException, NamingException {
        Connection con = DBConnectionProvider.getConn();
        ResultSet schemaUsers = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(
                    "select u.firstname firstname,u.lastname lastname,(address).addLine1 line1, (address).addLine2 line2, (address).addLine3 line3, "
                            + "(address).city city, (address).state, (address).phone phones, designation, c.divid divId, "
                            + "empNumber, b.name divname," + "a.createDate cdate, a.createBy cby, "
                            + "a.updateDate  udate,  a.updateBy uby, a.profileimage profileimage from master.users u ," + user.schemaName + ".userProfile a,"
                            + user.schemaName + ".divisions b," + user.schemaName + ".userdivmap c where u.id = ? AND u.id = a.userId and " + "c.divid = b.Id ");
            stmt.setInt(1, user.id);

            schemaUsers = stmt.executeQuery();
            if (schemaUsers != null) {
                schemaUsers.next();
                user.firstName = schemaUsers.getString("firstname");
                user.lastName = schemaUsers.getString("lastname");
                user.addLine1 = schemaUsers.getString("line1");
                user.addLine2 = schemaUsers.getString("line2");
                user.addLine3 = schemaUsers.getString("line3");
                user.city = schemaUsers.getString("city");
                user.state = schemaUsers.getString("state");
                user.phones = (String[]) schemaUsers.getArray("phones").getArray();
                user.designation = schemaUsers.getString("designation");
                user.divId = schemaUsers.getInt("divId");
                user.divName = schemaUsers.getString("divname");
                user.empNum = schemaUsers.getString("empnumber");
                user.createDate = schemaUsers.getDate("cdate");
                user.createBy = schemaUsers.getInt("cby");
                user.updateDate = schemaUsers.getDate("cdate");
                user.updateBy = schemaUsers.getInt("cby");
                user.profileImage = schemaUsers.getString("profileimage");
            }

        } finally {
            if (schemaUsers != null)
                if (!schemaUsers.isClosed())
                    schemaUsers.close();
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }

    }

    /***
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
     * @param profileImage
     * @param roleid
     * @param divid
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addUser(String firstname, String lastname, String username, boolean isActive,
                              String addLine1, String addLine2, String addLine3, String city, String state, String phones,
                              String designation, String empNumber, String profileImage, int roleid, String divid, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, User).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result;
            try {
                con.setAutoCommit(false);
                stmt = con.prepareStatement("select username from master.users where username = ?");
                stmt.setString(1, username);
                result = stmt.executeQuery();
                if (!result.next()) {

                    String password = generateRandomString();

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

                    stmt = con.prepareStatement("insert into master.users (firstname, lastname, "
                            + "clientId, username, password,isactive) values (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, firstname);
                    stmt.setString(2, lastname);
                    stmt.setInt(3, loggedInUser.clientId);
                    stmt.setString(4, username);
                    stmt.setString(5, sb.toString());
                    stmt.setBoolean(6, isActive);


                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0)
                        throw new SQLException("Create user failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int userid;
                    if (generatedKeys.next())
                        userid = generatedKeys.getInt(1);
                    else
                        throw new SQLException("Create user failed. No ID obtained");

                    phones = phones.replace('"', ' ');
                    phones = phones.replace('[', ' ');
                    phones = phones.replace(']', ' ');
                    phones = phones.replace('"', ' ');
                    phones = phones.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String phoneArr[] = phones.split(",");
                    Array pharr = con.createArrayOf("text", phoneArr);

                    stmt = con.prepareStatement("insert into " + loggedInUser.schemaName + ".userProfile(userId,"
                            + "address, designation, empNumber, createBy, updateDate, updateBy,profileimage) values (?, "
                            + "ROW(?,?,?,?,?,?), ?, ?,?,?,?,?)");
                    stmt.setInt(1, userid);
                    if (addLine1 != null || addLine1 != "")
                        stmt.setString(2, addLine1);
                    else
                        stmt.setString(2, null);

                    if (addLine2 != null || addLine2 != "")
                        stmt.setString(3, addLine2);
                    else
                        stmt.setString(3, null);
                    if (addLine3 != null || addLine3 != "")
                        stmt.setString(4, addLine3);
                    else
                        stmt.setString(4, null);
                    if (city != null || city != "")
                        stmt.setString(5, city);
                    else
                        stmt.setString(5, null);
                    if (state != null || state != "")
                        stmt.setString(6, state);
                    else
                        stmt.setString(6, null);
                    if (phones != null)
                        stmt.setArray(7, pharr);
                    else
                        stmt.setString(7, null);
                    if (designation != null || designation != "")
                        stmt.setString(8, designation);
                    else
                        stmt.setString(8, null);
                    if (empNumber != null || empNumber != "")
                        stmt.setString(9, empNumber);
                    else
                        stmt.setString(9, null);

                    stmt.setInt(10, loggedInUser.id);
                    stmt.setTimestamp(11, new Timestamp((new Date()).getTime()));
                    stmt.setInt(12, loggedInUser.id);
                    stmt.setString(13, profileImage);

                    stmt.executeUpdate();

                    // TODO: you need to check the return value here also and throw
                    // error if insert failed.

/*            if (node.has("roles")) {
               System.out.println("in if");
               JsonNode rolesNode = node.get("roles");
                Iterator<JsonNode> it = rolesNode.elements();
                System.out.println(it.hasNext());
                while (it.hasNext()) {
                    JsonNode role = it.next();*/
                    stmt = con.prepareStatement(
                            "insert into master.userroleMap (userId, roleId, effectDate,createBy) values"
                                    + "(?,?,?,?)");
                    System.out.println(stmt);
                    stmt.setInt(1, userid);
                    stmt.setInt(2, roleid);
                    stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                    stmt.setInt(4, loggedInUser.id);
                    stmt.executeUpdate();
                    System.out.println("role inserted");

                    // TODO: check the return and throw errors.
                    //}
//            } else
//                throw new RequiredDataMissing("Role is required");
                    System.out.println("DivId : " + divid);
                    divid = divid.replace('"', ' ');
                    divid = divid.replace('[', ' ');
                    divid = divid.replace(']', ' ');
                    divid = divid.replace('"', ' ');
                    divid = divid.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String[] divIdArr = divid.split(",");
                    for (int i = 0; i < divIdArr.length; i++) {
                        stmt = con.prepareStatement(
                                "insert into " + loggedInUser.schemaName + ".userdivmap (userId,divid,createdate,createBy) values"
                                        + "(?,?,?,?)");
                        stmt.setInt(1, userid);
                        stmt.setInt(2, Integer.parseInt(divIdArr[i]));
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setInt(4, loggedInUser.id);
                        stmt.executeUpdate();
                    }

                    generateAndSendEmail(username, "testrolla@gmail.com", "Rolla@test", firstname, lastname, password);
                    con.commit();
                    return userid;
                } else {
                    throw new BadRequestException("Email Id is already Exist");
                }
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }

        } else {
            new NotAuthorizedException("");
        }
        return 0;
    }

    /***
     * It gives all Users Which are not associate to any territory.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getDeassociateUser(int divId, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Read") ||
                Permissions.isAuthorised(userRole, User).equals("Write")) {

            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ArrayList<User> userList = new ArrayList<User>();

            try {
                // TODO: we need to check the div. the div also needs to be passed
                // as a parameter here. Territory active?

                stmt = con.prepareStatement("select a.userid id, (a.address).city city, c.username username , c.firstname firstname ," +
                        " c.lastname lastname " + "from "
                        + loggedInUser.schemaName + ".userProfile a left outer join " + loggedInUser.schemaName
                        + ".userTerritoryMap b " + " on (a.userId = b.userId) "
                        + " left join client1.userdivmap e on a.userid = e.userid,master.users c "
                        + " where b.terrId is null and c.id = a.userId AND c.isactive AND e.divid = ?");
                stmt.setInt(1, divId);

                result = stmt.executeQuery();
                while (result.next()) {
                    User user = new User();
                    user.id = result.getInt(1);
                    user.username = result.getString(3);
                    user.city = result.getString(2);
                    user.firstName = result.getString(4);
                    user.lastName = result.getString(5);
                    userList.add(user);
                }
            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return userList;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /**
     * Display All Users
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */

    public static List<User> getAllUsers(LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Read") ||
                Permissions.isAuthorised(userRole, User).equals("Write")) {

            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet divSet = null;
            ArrayList<User> userList = new ArrayList<User>();

            try {
                stmt = con.prepareStatement(
                        "select a.id id, a.clientId clientid,h.name clientname, a.firstname firstname, a.lastname lastname,a.isactive isActive, " +
                                " e.roleid roleid, f.name rolename, a.username username," +
                                " (b.address).addLine1 line1, (b.address).addLine2 line2, (b.address).addLine3 line3 ," +
                                " (b.address).city city, (b.address).state, (b.address).phone phones, b.designation ," +
                                " b.empNumber,b.createDate cdate, b.createBy cby ," +
                                " b.updateDate  udate,  b.updateBy uby,b.profileimage profileimage, (b1.address).city updatecity ," +
                                " (b1.address).state updatestate, (b1.address).phone updatephone ," +
                                "(select a1.username from master.users a1," +
                                " client1.userprofile b1 where a1.id = b1.updateby and b1.userId = a.id) updatename ,a1.firstname fname,a1.lastname lname " +
                                " from master.users a left join " +
                                loggedInUser.schemaName + ".userprofile b on a.id = b.userid " +
                                " left join master.userrolemap e on " +
                                " e.userid = a.id left join master.roles f on f.id = e.roleid " +
                                " left join master.clients h on h.id = a.clientId " +
                                " left join " + loggedInUser.schemaName + ".userprofile b1 on b1.userid = b.updateby " +
                                " left join master.users a1 on a1.id = b1.updateby " +
                                " order by b.updateDate DESC ");
                result = stmt.executeQuery();
                while (result.next()) {

                    User user = new User();
                    user.id = result.getInt("id");
                    user.clientId = result.getInt("clientid");
                    user.clientName = result.getString("clientname");
                    user.firstName = result.getString("firstname");
                    user.lastName = result.getString("lastname");
                    user.isActive = result.getBoolean("isActive");
                    user.roles = new ArrayList<Role>();
                    user.roles.add(new Role(result.getInt("roleid"), result.getString("rolename")));
                    user.username = result.getString("username");
                    user.addLine1 = result.getString("line1");
                    user.addLine2 = result.getString("line2");
                    user.addLine3 = result.getString("line3");
                    user.city = result.getString("city");
                    user.state = result.getString("state");
                    if (result.getArray("phones").getArray() == null)
                        user.phones = null;
                    else
                        user.phones = (String[]) result.getArray("phones").getArray();

                    user.roleid = result.getInt("roleid");
/*                    user.divId = result.getInt("divId");
                    user.divName = result.getString("divname");*/
                    user.empNum = result.getString("empnumber");
                    user.createDate = result.getTimestamp("cdate");
                    user.createBy = result.getInt("cby");
                    user.updateDate = result.getTimestamp("udate");
                    user.updateBy = result.getInt("uby");
                    user.designation = result.getString("designation");
                    user.userDetails = new ArrayList<>();
                    user.userDetails.add(new UserDetail(result.getInt("uby"), result.getString("updatename"), result.getString("fname"), result.getString("lname"), result.getString("updatecity"), result.getString("updatestate"), (String[]) result.getArray("updatephone").getArray()));
                    user.profileImage = result.getString("profileimage");
                    user.divisionName = new ArrayList<>();

                    stmt = con.prepareStatement(" SELECT u.divid,d.name FROM "+loggedInUser.schemaName+".userdivmap u " +
                            " left join "+loggedInUser.schemaName+".divisions d on d.id = u.divid" +
                            " WHERE userid = ? ");
                    stmt.setInt(1,result.getInt("id"));
                    divSet = stmt.executeQuery();
                    while (divSet.next())
                    {
                        user.divisionName.add(divSet.getString(2));
                    }

                    userList.add(user);
                }
            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return userList;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     * This method Display all Users by specific division.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getAllUsersByDivId(int id, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Read") || Permissions.isAuthorised(userRole, User).equals("Write")) {

            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ArrayList<User> userList = new ArrayList<User>();

            try {
                if (id == -1) {
                    userList = (ArrayList<User>) getAllUsers(loggedInUser);
                } else {
                    stmt = con.prepareStatement(
                            "select a.id id, a.clientId clientid,h.name clientname, a.firstname firstname, a.lastname lastname,a.isactive isActive, " +
                                    " e.roleid roleid, f.name rolename, a.username username," +
                                    " (b.address).addLine1 line1, (b.address).addLine2 line2, (b.address).addLine3 line3 ," +
                                    " (b.address).city city, (b.address).state, (b.address).phone phones, b.designation ," +
                                    " c.divid divId , b.empNumber, d.name divname,b.createDate cdate, b.createBy cby ," +
                                    " b.updateDate  udate,  b.updateBy uby, b.profileimage profileimage , (b1.address).city updatecity," +
                                    " (b1.address).state updatestate, (b1.address).phone updatephone , " +
                                    " (select a1.username  " +
                                    " from master.users a1," +
                                    " " + loggedInUser.schemaName + ".userprofile b1 where a1.id = b1.updateby and b1.userId = a.id) updatename ," +
                                    " a1.firstname fname ,a1.lastname lname " +
                                    " from master.users a left join " +
                                    loggedInUser.schemaName + ".userprofile b on a.id = b.userid " +
                                    " left join " + loggedInUser.schemaName + ".userdivmap c on a.id = c.userid left join " +
                                    loggedInUser.schemaName + ".divisions d on d.id = c.divid left join master.userrolemap e on " +
                                    " e.userid = a.id left join master.roles f on f.id = e.roleid  " +
                                    " left join master.clients h on h.id = a.clientId " +
                                    " left join " + loggedInUser.schemaName + ".userprofile b1 on b1.userid = b.updateby " +
                                    " left join master.users a1 on a1.id = b1.updateby " +
                                    " where c.divid = ? order by b.updateDate DESC ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    while (result.next()) {

                        User user = new User();
                        user.id = result.getInt("id");
                        user.clientId = result.getInt("clientid");
                        user.clientName = result.getString("clientname");
                        user.firstName = result.getString("firstname");
                        user.lastName = result.getString("lastname");
                        user.isActive = result.getBoolean("isActive");
                        user.roles = new ArrayList<Role>();
                        user.roles.add(new Role(result.getInt("roleid"), result.getString("rolename")));
                        user.username = result.getString("username");
                        user.addLine1 = result.getString("line1");
                        user.addLine2 = result.getString("line2");
                        user.addLine3 = result.getString("line3");
                        user.city = result.getString("city");
                        user.state = result.getString("state");
                        user.phones = (String[]) result.getArray("phones").getArray();
                        user.roleid = result.getInt("roleid");
                        user.divId = result.getInt("divId");
                        user.divName = result.getString("divname");
                        user.empNum = result.getString("empnumber");
                        user.createDate = result.getTimestamp("cdate");
                        user.createBy = result.getInt("cby");
                        user.updateDate = result.getTimestamp("udate");
                        user.updateBy = result.getInt("uby");
                        user.designation = result.getString("designation");
                        user.userDetails = new ArrayList<>();
                        user.userDetails.add(new UserDetail(result.getInt("uby"), result.getString("updatename"), result.getString("fname"), result.getString("lname"), result.getString("updatecity"), result.getString("updatestate"), (String[]) result.getArray("updatephone").getArray()));
                        user.profileImage = result.getString("profileimage");
                        userList.add(user);
                    }
                }
            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return userList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method Display all Users by specific division.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getAllActivateUsersByDivId(int id, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Read") || Permissions.isAuthorised(userRole, User).equals("Write")) {

            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ArrayList<User> userList = new ArrayList<User>();

            try {

                stmt = con.prepareStatement(
                        "select a.id id, a.clientId clientid,h.name clientname, a.firstname firstname, a.lastname lastname,a.isactive isActive, " +
                                " e.roleid roleid, f.name rolename, a.username username," +
                                " (b.address).addLine1 line1, (b.address).addLine2 line2, (b.address).addLine3 line3 ," +
                                " (b.address).city city, (b.address).state, (b.address).phone phones, b.designation ," +
                                " c.divid divId , b.empNumber, d.name divname,b.createDate cdate, b.createBy cby ," +
                                " b.updateDate  udate,  b.updateBy uby, b.profileimage profileimage , (b1.address).city updatecity," +
                                " (b1.address).state updatestate, (b1.address).phone updatephone , " +
                                " (select a1.username  " +
                                " from master.users a1," +
                                " " + loggedInUser.schemaName + ".userprofile b1 where a1.id = b1.updateby and b1.userId = a.id) updatename ," +
                                " a1.firstname fname ,a1.lastname lname " +
                                " from master.users a left join " +
                                loggedInUser.schemaName + ".userprofile b on a.id = b.userid " +
                                " left join " + loggedInUser.schemaName + ".userdivmap c on a.id = c.userid left join " +
                                loggedInUser.schemaName + ".divisions d on d.id = c.divid left join master.userrolemap e on " +
                                " e.userid = a.id left join master.roles f on f.id = e.roleid  " +
                                " left join master.clients h on h.id = a.clientId " +
                                " left join " + loggedInUser.schemaName + ".userprofile b1 on b1.userid = b.updateby " +
                                " left join master.users a1 on a1.id = b1.updateby " +
                                " where c.divid = ? AND a.isactive order by b.updateDate DESC ");
                stmt.setInt(1, id);
                result = stmt.executeQuery();
                while (result.next()) {

                    User user = new User();
                    user.id = result.getInt("id");
                    user.clientId = result.getInt("clientid");
                    user.clientName = result.getString("clientname");
                    user.firstName = result.getString("firstname");
                    user.lastName = result.getString("lastname");
                    user.isActive = result.getBoolean("isActive");
                    user.roles = new ArrayList<Role>();
                    user.roles.add(new Role(result.getInt("roleid"), result.getString("rolename")));
                    user.username = result.getString("username");
                    user.addLine1 = result.getString("line1");
                    user.addLine2 = result.getString("line2");
                    user.addLine3 = result.getString("line3");
                    user.city = result.getString("city");
                    user.state = result.getString("state");
                    user.phones = (String[]) result.getArray("phones").getArray();
                    user.roleid = result.getInt("roleid");
                    user.divId = result.getInt("divId");
                    user.divName = result.getString("divname");
                    user.empNum = result.getString("empnumber");
                    user.createDate = result.getTimestamp("cdate");
                    user.createBy = result.getInt("cby");
                    user.updateDate = result.getTimestamp("udate");
                    user.updateBy = result.getInt("uby");
                    user.designation = result.getString("designation");
                    user.userDetails = new ArrayList<>();
                    user.userDetails.add(new UserDetail(result.getInt("uby"), result.getString("updatename"), result.getString("fname"), result.getString("lname"), result.getString("updatecity"), result.getString("updatestate"), (String[]) result.getArray("updatephone").getArray()));
                    user.profileImage = result.getString("profileimage");
                    userList.add(user);
                }
            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return userList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  Method is used to get all ROOT level user details.
     *
     * @param divId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getAllRootLevelUser(int divId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,User).equals("Read") ||
                Permissions.isAuthorised(userRole,User).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<User> userList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            int roleId = 0;
            String roleName = "";

            try
            {
                stmt = con.prepareStatement("SELECT id, name FROM master.roles WHERE name = ? ");
                stmt.setString(1,"ROOT");
                resultSet = stmt.executeQuery();

                while (resultSet.next())
                {
                    roleId = resultSet.getInt(1);
                    roleName = resultSet.getString(2);
                }

                stmt = con.prepareStatement(" select a.id id, a.clientId clientid,h.name clientname, a.firstname firstname, " +
                        " a.lastname lastname,a.isactive isActive, " +
                        " e.roleid roleid, f.name rolename, a.username username, " +
                        " (b.address).addLine1 line1, (b.address).addLine2 line2, (b.address).addLine3 line3 , " +
                        " (b.address).city city, (b.address).state, (b.address).phone phones, b.designation , " +
                        " c.divid divId , b.empNumber, d.name divname,b.createDate cdate, b.createBy cby , " +
                        " b.updateDate  udate,  b.updateBy uby, b.profileimage profileimage " +
                        " from master.users a " +
                        " left join "+schemaname+".userprofile b on a.id = b.userid " +
                        " left join "+schemaname+".userdivmap c on a.id = c.userid " +
                        " left join "+schemaname+".divisions d on d.id = c.divid " +
                        " left join master.userrolemap e on e.userid = a.id " +
                        " left join master.roles f on f.id = e.roleid " +
                        " left join master.clients h on h.id = a.clientId " +
                        " WHERE c.divid = ? AND a.isactive AND f.id = ? AND f.name = ? order by b.updateDate DESC ");
                stmt.setInt(1,divId);
                stmt.setInt(2,roleId);
                stmt.setString(3,roleName);

                resultSet = stmt.executeQuery();
                while (resultSet.next())
                {
                    User user = new User();
                    user.id = resultSet.getInt("id");
                    user.clientId = resultSet.getInt("clientid");
                    user.clientName = resultSet.getString("clientname");
                    user.firstName = resultSet.getString("firstname");
                    user.lastName = resultSet.getString("lastname");
                    user.isActive = resultSet.getBoolean("isActive");
                    user.roles = new ArrayList<Role>();
                    user.roles.add(new Role(resultSet.getInt("roleid"), resultSet.getString("rolename")));
                    user.username = resultSet.getString("username");
                    user.addLine1 = resultSet.getString("line1");
                    user.addLine2 = resultSet.getString("line2");
                    user.addLine3 = resultSet.getString("line3");
                    user.city = resultSet.getString("city");
                    user.state = resultSet.getString("state");
                    user.phones = (String[]) resultSet.getArray("phones").getArray();
                    user.roleid = resultSet.getInt("roleid");
                    user.divId = resultSet.getInt("divId");
                    user.divName = resultSet.getString("divname");
                    user.empNum = resultSet.getString("empnumber");
                    user.createDate = resultSet.getDate("cdate");
                    user.createBy = resultSet.getInt("cby");
                    user.updateDate = resultSet.getDate("udate");
                    user.updateBy = resultSet.getInt("uby");
                    user.designation = resultSet.getString("designation");
                    user.profileImage = resultSet.getString("profileimage");
                    userList.add(user);
                }
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return userList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method allows user to deactivate User from Database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deactivateUser(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            javax.naming.Context env = null;
            env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");


            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("UPDATE "
                            + "master.users SET isactive = ? WHERE id = ?");

                    stmt.setBoolean(1, node.get("isactive").asBoolean());
                    stmt.setInt(2, node.get("id").asInt());
                    result = stmt.executeUpdate();
                    if (node.get("isactive").asBoolean() == false) {
                        int accessTimeout = 0;

                        if (node.get("isPublic").asBoolean()) {

                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_PUBLIC_TIMEOUT");
                            Mem.setData(node.get("id").asInt() + "#DEACTIVATED", accessTimeout * 2);

                        } else {
                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_WORK_TIMEOUT");
                            Mem.setData(node.get("id").asInt() + "#DEACTIVATED", accessTimeout * 2);

                        }
                    }
                } else
                    throw new Exception("DB connection is null");
            } finally {

                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return result;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method allows to update user details.
     *
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
     * @param profileImage
     * @param roleid
     * @param divid
     * @param userid
     * @param isPublic
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateUserDetails(String firstname, String lastname, String username, boolean isActive,
                                        String addLine1, String addLine2, String addLine3, String city, String state, String phones,
                                        String designation, String empNumber, String profileImage, int roleid, String divid,
                                        int userid, boolean isPublic, LoggedInUser loggedInUser)
            throws Exception {

        // TODO: check if the user has rights to perform this action.
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, User).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt;
            ResultSet result;
            int affectedRows;
            int accessTimeout = 0;
            javax.naming.Context env = null;
            env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");

            try {
                con.setAutoCommit(false);
                stmt = con.prepareStatement("UPDATE master.users SET firstname = ?,lastname = ?,username=?, isactive=? " +
                        " WHERE id = ?");
                stmt.setString(1, firstname);
                stmt.setString(2, lastname);
                stmt.setString(3, username);
                stmt.setBoolean(4, isActive);
                stmt.setInt(5, userid);

                affectedRows = stmt.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Update user failed.");
                else {

                    phones = phones.replace('"', ' ');
                    phones = phones.replace('[', ' ');
                    phones = phones.replace(']', ' ');
                    phones = phones.replace('"', ' ');
                    phones = phones.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String phoneArr[] = phones.split(",");
                    Array pharr = con.createArrayOf("text", phoneArr);

                    stmt = con.prepareStatement("UPDATE " + loggedInUser.schemaName + ".userProfile SET"
                            + " address = ROW(?,?,?,?,?,?), designation = ?, empNumber = ?, updateDate= ?, updateBy= ? , profileimage = ? where userid = ?");

                    System.out.println(stmt.toString());

                    stmt.setString(1, addLine1);
                    stmt.setString(2, addLine2);
                    stmt.setString(3, addLine3);
                    stmt.setString(4, city);
                    stmt.setString(5, state);
                    stmt.setArray(6, pharr);
                    stmt.setString(7, designation);
                    stmt.setString(8, empNumber);
                    stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                    stmt.setInt(10, loggedInUser.id);
                    stmt.setString(11, profileImage);
                    stmt.setInt(12, userid);

                    stmt.executeUpdate();

                    stmt = con.prepareStatement("DELETE from " + loggedInUser.schemaName + ".userdivmap where userid=?");
                    stmt.setInt(1, userid);
                    stmt.executeUpdate();

                    divid = divid.replace('"', ' ');
                    divid = divid.replace('[', ' ');
                    divid = divid.replace(']', ' ');
                    divid = divid.replace('"', ' ');
                    divid = divid.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String[] divIdArr = divid.split(",");

                    for (int i = 0; i < divIdArr.length; i++) {
                        stmt = con.prepareStatement(
                                "insert into " + loggedInUser.schemaName + ".userdivmap (userId,divid,createdate,createBy) values"
                                        + "(?,?,?,?)");
                        stmt.setInt(1, userid);
                        stmt.setInt(2, Integer.parseInt(divIdArr[i]));
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setInt(4, loggedInUser.id);
                        stmt.executeUpdate();
                    }

                    stmt = con.prepareStatement("SELECT roleid from master.userrolemap where userid=?");
                    stmt.setInt(1, userid);
                    result = stmt.executeQuery();

                    stmt = con.prepareStatement("UPDATE master.userrolemap SET roleid=? where userid=?");

                    if (result.next()) {
                        stmt.setInt(1, roleid);
                        stmt.setInt(2, userid);
                        if (result.getInt("roleid") != roleid) {
                            stmt.executeUpdate();

                            if (isPublic) {

                                accessTimeout = (int) env.lookup("ACCESS_TOKEN_PUBLIC_TIMEOUT");
                                Mem.setData(username + "#ROLECHANGED", accessTimeout * 2);

                            } else {
                                accessTimeout = (int) env.lookup("ACCESS_TOKEN_WORK_TIMEOUT");
                                Mem.setData(username + "#ROLECHANGED", accessTimeout * 2);

                            }

                            stmt = con.prepareStatement("insert into master.userrolemaphistory (userid, roleid, effectdate, createdate, createby) values (?,?,?,?,?)");
                            stmt.setInt(1, userid);
                            stmt.setInt(2, roleid);
                            stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                            stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                            stmt.setInt(5, loggedInUser.id);

                            affectedRows = stmt.executeUpdate();

//                        throw new Exception(new NotAuthorizedException("You Must Login Again..."));
                        }
                    }

                }

                con.commit();
                return affectedRows;
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     * @param firstname
     * @param lastname
     * @param addLine1
     * @param addLine2
     * @param addLine3
     * @param city
     * @param state
     * @param phones
     * @param profileImage
     * @param userid
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateUserProfileDetails(String firstname, String lastname,
                                               String addLine1, String addLine2, String addLine3, String city, String state, String phones,
                                               String profileImage,
                                               int userid, LoggedInUser loggedInUser)
            throws Exception {

        // TODO: check if the user has rights to perform this action.
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, User).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt;
            int affectedRows;

            try {
                con.setAutoCommit(false);
                stmt = con.prepareStatement("UPDATE master.users SET firstname = ?,lastname = ? " +
                        " WHERE id = ?");
                System.out.println("First name : " + firstname);
                System.out.println("Last name : " + lastname);
                stmt.setString(1, firstname);
                stmt.setString(2, lastname);
                stmt.setInt(3, userid);

                affectedRows = stmt.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Update user failed.");
                else {

                    phones = phones.replace('"', ' ');
                    phones = phones.replace('[', ' ');
                    phones = phones.replace(']', ' ');
                    phones = phones.replace('"', ' ');
                    phones = phones.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    String phoneArr[] = phones.split(",");
                    Array pharr = con.createArrayOf("text", phoneArr);

                    stmt = con.prepareStatement("UPDATE " + loggedInUser.schemaName + ".userProfile SET"
                            + " address = ROW(?,?,?,?,?,?) , profileimage = ? where userid = ?");

                    stmt.setString(1, addLine1);
                    stmt.setString(2, addLine2);
                    stmt.setString(3, addLine3);
                    stmt.setString(4, city);
                    stmt.setString(5, state);
                    stmt.setArray(6, pharr);
                    stmt.setString(7, profileImage);
                    stmt.setInt(8, userid);

                    stmt.executeUpdate();
                }

                con.commit();
                return affectedRows;
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }

    }


    /***
     * Method used to give all Clients which are active
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getAllClients(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, User).equals("Read") ||
                Permissions.isAuthorised(userRole, User).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<User> users = new ArrayList<User>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, name, isactive, schemaname FROM master.clients where isactive");
                    result = stmt.executeQuery();
                    while (result.next()) {
                        User user = new User();
                        user.clientId = result.getInt(1);
                        user.clientName = result.getString(2);
                        user.isActive = result.getBoolean(3);
                        user.schemaName = result.getString(4);
                        users.add(user);
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return users;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method used to get all divisions for logged in user from userdivmap table.
     *
     * @param userId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getDivisions(int userId, LoggedInUser loggedInUser) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        ArrayList<User> list = new ArrayList<User>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        User user = null;
        try {

            stmt = con.prepareStatement("SELECT u.divid,d.name from " + loggedInUser.schemaName + ".userdivmap u " +
                    " left join " + loggedInUser.schemaName + ".divisions d on d.id = u.divid" +
                    " where u.userid = ? ");
            stmt.setInt(1, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                user = new User();
                user.divId = result.getInt(1);
                user.divName = result.getString(2);
                list.add(user);
            }

        } finally {
            if (result != null)
                if (!result.isClosed())
                    result.close();
            if (stmt != null)
                if (!stmt.isClosed())
                    stmt.close();
            if (con != null)
                if (!con.isClosed())
                    con.close();
        }
        return list;
    }

    /***
     *  Method is used to get device details of User.
     *
     * @param userId
     * @return
     * @throws Exception
     */
    public static String getDeviceDetails(int userId) throws Exception
    {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        List<String> deviceList = new ArrayList<>();
        ResultSet result = null;
        String device = "";

        try {
            stmt = con.prepareStatement(" SELECT devicetoken,deviceos,firstname,lastname FROM master.users WHERE id = ? ");
            stmt.setInt(1,userId);
            result = stmt.executeQuery();
            while (result.next())
            {
               device = result.getString(1)+","+result.getString(2)
                       +","+result.getString(3)+","+result.getString(4);
            }
        }
        finally {
            if(con != null)
                if(!con.isClosed())
                    con.close();
            if(stmt != null)
                if(!stmt.isClosed())
                    stmt.close();
            if(result != null)
                if(!result.isClosed())
                    result.close();

        }
        return device;
    }

    /***
     *  Method is used to remove devicetoken and deviceos of used when used get logs out from application.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeDeviceDetails(LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,User).equals("Read") ||
                Permissions.isAuthorised(userRole,User).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;

            try {
                stmt = con.prepareStatement(" UPDATE master.users SET devicetoken = ? , deviceos = ? " +
                        " WHERE id = ?");
                stmt.setString(1,"");
                stmt.setString(2,null);
                stmt.setInt(3,loggedInUser.id);
                affectedRows = stmt.executeUpdate();
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
            }
            return affectedRows;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  Method is used to update Device Token and Device OS details in database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateDeviceDetails(JsonNode node,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,User).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;

            try {
                stmt = con.prepareStatement(" UPDATE master.users SET devicetoken = ? , deviceos = ? " +
                        " WHERE id = ?");
                stmt.setString(1,node.get("deviceToken").asText());
                stmt.setString(2,node.get("deviceOS").asText());
                stmt.setInt(3,loggedInUser.id);
                affectedRows = stmt.executeUpdate();
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
            }
            return affectedRows;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
    /***
     * Method used to send Mail to User
     *
     * @param username
     * @param from
     * @param fromPassword
     * @param firstname
     * @param lastname
     * @param newPassword
     * @return
     * @throws MessagingException
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static boolean generateAndSendEmail(String username, String from, String fromPassword, String firstname, String lastname, String newPassword) throws MessagingException, SQLException, NamingException, ClassNotFoundException {
        Properties mailServerProperties;
        Session getMailSession;
        MimeMessage generateMailMessage;
        // Step1
//        System.out.println("\n 1st ===> setup Mail Server Properties..");
        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");

        // Step2
//        System.out.println("\n\n 2nd ===> get Mail Session..");
        getMailSession = Session.getDefaultInstance(mailServerProperties, null);
        generateMailMessage = new MimeMessage(getMailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
        generateMailMessage.setSubject("Rolla Password..");
//        String emailBody = generateRandomString();
//        System.out.println(generateRandomString());
        String emailBody = "<h3> Hi " + firstname + " " + lastname + ", </h3>" + " <h4> New Rolla account is registered with " + username + " </h4>" + "<h4> Please Use this password for first time login </h4>" + "<h3> Password : " + newPassword + " </h3>";
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");

        // Step3
//        System.out.println("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        transport.connect("smtp.gmail.com", from, fromPassword);
        if (transport.isConnected()) {
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
            return true;
        } else {
            return false;
        }
    }

    // save uploaded file to new location
    public static String writeToFile(InputStream inputStream, String fileName)
            throws IOException {

        String existingBucketName = "com.brewconsulting.client1";
        String finalUrl = null;
        String amazonFileUploadLocationOriginal = existingBucketName
                + "/Profile";

        try {

            AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJZZRFGQGNZIDUFTQ", "12uUP7pQrvR3Kf0GpyeJr328RQ/a1m8TI+/8w2X8");
            AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    amazonFileUploadLocationOriginal, fileName, inputStream,
                    objectMetadata);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("Etag:" + result.getETag() + "-->" + result);

            finalUrl = "https://s3.amazonaws.com/"
                    + amazonFileUploadLocationOriginal + "/" + fileName;
            System.out.println(finalUrl);

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        return finalUrl;
    }


// userDeviceIdKey is the device id you will query from your database

        public static void pushFCMNotification(JsonNode node) throws Exception{

            String authKey = node.get("apiKey").asText(); // You FCM AUTH key
            String FMCurl = "https://fcm.googleapis.com/fcm/send";

            URL url = new URL(FMCurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","key="+authKey);
            conn.setRequestProperty("Content-Type","application/json");

            JSONObject json = new JSONObject();
            json.put("to",node.get("DeviceId").asText().trim());
            JSONObject info = new JSONObject();
            info.put("title", "Notificatoin Title"); // Notification title
            info.put("body", "Hello Test notification"); // Notification body
            json.put("notification", info);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();
        }
}


