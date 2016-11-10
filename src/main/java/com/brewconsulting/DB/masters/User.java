
package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.exceptions.RequiredDataMissing;
import com.brewconsulting.login.Credentials;
import com.brewconsulting.masters.Mem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;

public class User {

    @JsonProperty("clientId")
    @JsonView({UserViews.authView.class, UserViews.clientView.class })
    public int clientId; // -1 means no client

    @JsonProperty("id")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class })
    public int id;

    @JsonProperty("username")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class})
    public String username;

    @JsonView({UserViews.authView.class, UserViews.clientView.class })
    @JsonProperty("schemaName")
    public String schemaName;

    @JsonView(UserViews.authView.class)
    @JsonProperty("firstName")
    public String firstName;

    @JsonView(UserViews.authView.class)
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

    @JsonView(UserViews.profileView.class)
    @JsonProperty("divId")
    public int divId;

    @JsonView(UserViews.profileView.class)
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
    public Date createDate;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("createBy")
    public int createBy;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("updateDate")
    public Date updateDate;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("updateBy")
    public int updateBy;

    @JsonView({UserViews.profileView.class , UserViews.clientView.class})
    @JsonProperty("isActive")
    public boolean isActive;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("designation")
    public String designation;

    @JsonView(UserViews.profileView.class)
    @JsonProperty("updatedName")
    public String updatedName;

    @JsonView({UserViews.profileView.class , UserViews.clientView.class})
    @JsonProperty("clientName")
    public String clientName;

    @JsonView(UserViews.authView.class)
    @JsonProperty("isFirstLogin")
    public boolean isFirstLogin;

    // make the constructor private.
    protected User() {

    }


    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 8;

    /**
     * This method generates random string
     * @return
     */
    public static String generateRandomString()
    {

        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<RANDOM_STRING_LENGTH; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    /**
     * This method generates random numbers
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
     *  Method used to change password of user.
     *
     * @param id
     * @param node
     * @return
     * @throws Exception
     */
    public static int changePassword(int id,JsonNode node) throws Exception
    {


        Connection con = DBConnectionProvider.getConn();
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

        try
        {
            PreparedStatement stmt = con.prepareStatement("Update master.users set password = ? where id = ?");
            stmt.setString(1,sb.toString());
            stmt.setInt(2,id);
            affectedRows = stmt.executeUpdate();

            stmt = con.prepareStatement("Update master.users set isfirstlogin = ? where id = ?");
            stmt.setBoolean(1,false);
            stmt.setInt(2,id);
            affectedRows = stmt.executeUpdate();
        } finally {
            if (con != null)
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
        boolean isActive;
        User user = null;

        try {

            PreparedStatement stmt = con.prepareStatement(
                    "select a.isActive"
                            + " from master.users a where a.id=?");
            stmt.setInt(1, loggedInUser.id);
            ResultSet resultSet = stmt.executeQuery();

            boolean data = Mem.getData(loggedInUser.id + "#DEACTIVATED");

            if (resultSet.next()) {
                isActive = resultSet.getBoolean(1);

                if (isActive) {

                    stmt = con.prepareStatement(
                            "select a.id, a.clientId, a.firstName, a.lastName, schemaName, d.id roleid, d.name rolename, a.username "
                                    + " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
                                    + " where a.isActive  and a.clientId = b.id and "
                                    + " a.id = c.userId and c.roleId = d.id and a.id = ?");

                    stmt.setInt(1,loggedInUser.id);

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null)
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
    public static User authenticate(String username, String password)
            throws ClassNotFoundException, SQLException, NamingException {
        User user = null;
        Connection con = DBConnectionProvider.getConn();
        try {

            PreparedStatement stmt = con.prepareStatement(
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
            boolean isAllowed = false;

            for (Role role : loggedInUser.roles) {
                if (Permissions.isAuthorised(role.roleId, Permissions.USER_PROFILE, Permissions.READ_ONLY)) {
                    isAllowed = true;
                    break;
                }
            }

            if (!isAllowed)
                throw new NotAuthorizedException("");

            // user = new UserProfile(find(id));
            user = fillFullProfile(loggedInUser, id);
        } else {
            user = new User();
            user.id = loggedInUser.id;
            user.clientId = loggedInUser.clientId;
            user.schemaName = loggedInUser.schemaName;
            user.firstName = loggedInUser.firstName;
            user.lastName = loggedInUser.lastName;
            user.username = loggedInUser.username;
            user.roles = new ArrayList<Role>();
            for (Role role : loggedInUser.roles) {
                user.roles.add(role);
            }
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
            throws ClassNotFoundException, SQLException, NamingException {
        Connection con = DBConnectionProvider.getConn();
        ResultSet schemaUsers = null;
        PreparedStatement stmt = null;
        User userProfile = null;
        try {
            stmt = con.prepareStatement(
                    "select a.id id, a.clientId clientid,h.name clientname , a.firstname firstname, a.lastname lastname, h.schemaName schemaname,"
                            + "c.id roleid, c.name rolename, a.username username, "
                            + "(d.address).addLine1 line1, (d.address).addLine2 line2, (d.address).addLine3 line3,"
                            + "(d.address).city city, (d.address).state, (d.address).phone phones, d.designation, g.divId divId,"
                            + "empNumber, e.name divname," + "d.createDate cdate, d.createBy cby, "
                            + "d.updateDate  udate,  d.updateBy uby"
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
                    userProfile.designation = schemaUsers.getString("designation");
                    continue;
                }
                userProfile.roles.add(new Role(schemaUsers.getInt("roleid"), schemaUsers.getString("rolename")));

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
        return userProfile;
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
                    "select (address).addLine1 line1, (address).addLine2 line2, (address).addLine3 line3, "
                            + "(address).city city, (address).state, (address).phone phones, designation, c.divid divId, "
                            + "empNumber, b.name divname," + "a.createDate cdate, a.createBy cby, "
                            + "a.updateDate  udate,  a.updateBy uby from " + user.schemaName + ".userProfile a,"
                            + user.schemaName + ".divisions b," + user.schemaName + ".userdivmap c where a.userId = ? and " + "c.divid = b.Id ");
            stmt.setInt(1, user.id);

            schemaUsers = stmt.executeQuery();
            if (schemaUsers != null) {
                schemaUsers.next();
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
     * Create New User
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws RequiredDataMissing
     * @throws NamingException
     */
    public static int createUser(JsonNode node, LoggedInUser loggedInUser)
            throws ClassNotFoundException, SQLException, RequiredDataMissing, NamingException, MessagingException {

        // TODO: check if the user has rights to perform this action.

        boolean isAllowed = false;

        for (Role role : loggedInUser.roles) {
            if (Permissions.isAuthorised(role.roleId, Permissions.USER_PROFILE, Permissions.READ_ONLY)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed)
            throw new NotAuthorizedException("");

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result;

        try {

            con.setAutoCommit(false);

            stmt = con.prepareStatement("select username from master.users where username = ?");
            stmt.setString(1,node.get("username").asText());
            result = stmt.executeQuery();
            if(!result.next()) {

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
                stmt.setString(1, node.get("firstName").asText());
                stmt.setString(2, node.get("lastName").asText());
                stmt.setInt(3, node.get("clientId").asInt());
                stmt.setString(4, node.get("username").asText());
                stmt.setString(5, sb.toString());
                stmt.setBoolean(6,node.get("isActive").asBoolean());


                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0)
                    throw new SQLException("Create user failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int userid;
                if (generatedKeys.next())
                    userid = generatedKeys.getInt(1);
                else
                    throw new SQLException("Create user failed. No ID obtained");

                // TODO: set up the phones string array
                String[] phoneArr = new String[node.withArray("phones").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("phones").size(); i++) {
                    phoneArr[i] = node.withArray("phones").get(i).asText();
                }

                Array pharr = con.createArrayOf("text", phoneArr);

                stmt = con.prepareStatement("insert into " + loggedInUser.schemaName + ".userProfile(userId,"
                        + "address, designation, empNumber, createBy, updateDate, updateBy) values (?, "
                        + "ROW(?,?,?,?,?,?), ?, ?,?,?,?)");
                stmt.setInt(1, userid);
                if (node.has("addLine1"))
                    stmt.setString(2, node.get("addLine1").asText());
                else
                    stmt.setString(2, null);

                if (node.has("addLine2"))
                    stmt.setString(3, node.get("addLine2").asText());
                else
                    stmt.setString(3, null);
                if (node.has("addLine3"))
                    stmt.setString(4, node.get("addLine3").asText());
                else
                    stmt.setString(4, null);
                if (node.has("city"))
                    stmt.setString(5, node.get("city").asText());
                else
                    stmt.setString(5, null);
                if (node.has("state"))
                    stmt.setString(6, node.get("state").asText());
                else
                    stmt.setString(6, null);
                if (node.has("phones"))
                    stmt.setArray(7, pharr);
                else
                    stmt.setString(7, null);
                if (node.has("designation"))
                    stmt.setString(8, node.get("designation").asText());
                else
                    stmt.setString(8, null);
                if (node.has("empNumber"))
                    stmt.setString(9, node.get("empNumber").asText());
                else
                    stmt.setString(9, null);

                stmt.setInt(10, loggedInUser.id);
                stmt.setTimestamp(11, new Timestamp((new Date()).getTime()));
                stmt.setInt(12, loggedInUser.id);

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
                stmt.setInt(2, node.get("roleid").asInt());
                stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                stmt.setInt(4, loggedInUser.id);
                stmt.executeUpdate();
                System.out.println("role inserted");
                // TODO: check the return and throw errors.
                //}
//            } else
//                throw new RequiredDataMissing("Role is required");

                stmt = con.prepareStatement(
                        "insert into " + loggedInUser.schemaName + ".userdivmap (userId,divid,createdate,createBy) values"
                                + "(?,?,?,?)");
                stmt.setInt(1, userid);
                stmt.setInt(2, node.get("divId").asInt());
                stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                stmt.setInt(4, loggedInUser.id);
                stmt.executeUpdate();
                generateAndSendEmail(node.get("username").asText(),"testrolla@gmail.com","Rolla@test",password);
                con.commit();
                return userid;
            }
            else
            {
                return 0;
            }
        } catch (Exception ex) {
            if (con != null)
                con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(false);
            if (con != null)
                con.close();
        }
    }

    /***
     * It gives all Users Which are not associate to any territory.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getDeassociateUser(int divId ,LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (!Permissions.isAuthorised(userRole, Permissions.USER_PROFILE, Permissions.getAccessLevel(userRole)))
            throw new NotAuthorizedException("");

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        ArrayList<User> userList = new ArrayList<User>();

        try {
            // TODO: we need to check the div. the div also needs to be passed
            // as a parameter here. Territory active?

            stmt = con.prepareStatement("select a.userid id, (a.address).city city, c.username username " + "from "
                    + loggedInUser.schemaName + ".userProfile a left outer join " + loggedInUser.schemaName
                    + ".userTerritoryMap b " + " on (a.userId = b.userId) "
                    + " left join client1.userdivmap e on a.userid = e.userid,master.users c "
                    + " where b.terrId is null and c.id = a.userId AND c.isactive AND e.divid = ?");
            stmt.setInt(1,divId);

            result = stmt.executeQuery();
            while (result.next()) {
                User user = new User();
                user.id = result.getInt(1);
                user.username = result.getString(3);
                user.city = result.getString(2);
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

    }

    /**
     *  Display All Users
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */

    public static List<User> getAllUsers(LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (!Permissions.isAuthorised(userRole, Permissions.USER_PROFILE, Permissions.getAccessLevel(userRole)))
            throw new NotAuthorizedException("");

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
                            " b.updateDate  udate,  b.updateBy uby,(select a1.username from master.users a1," +
                            " client1.userprofile b1 where a1.id = b1.updateby and b1.userId = a.id) updatename " +
                            " from master.users a left join " +
                            loggedInUser.schemaName + ".userprofile b on a.id = b.userid " +
                            " left join " + loggedInUser.schemaName + ".userdivmap c on a.id = c.userid left join " +
                            loggedInUser.schemaName + ".divisions d on d.id = c.divid left join master.userrolemap e on " +
                            " e.userid = a.id left join master.roles f on f.id = e.roleid " +
                            " left join master.clients h on h.id = a.clientId " +
                            "order by a.id asc ");
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
                if(result.getArray("phones").getArray() == null)
                    user.phones = null;
                else
                user.phones = (String[]) result.getArray("phones").getArray();


                user.roleid = result.getInt("roleid");
                user.divId = result.getInt("divId");
                user.divName = result.getString("divname");
                user.empNum = result.getString("empnumber");
                user.createDate = result.getDate("cdate");
                user.createBy = result.getInt("cby");
                user.updateDate = result.getDate("cdate");
                user.updateBy = result.getInt("cby");
                user.designation = result.getString("designation");
                user.updatedName = result.getString("updatename");
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

        if (!Permissions.isAuthorised(userRole, Permissions.USER_PROFILE, Permissions.getAccessLevel(userRole)))
            throw new NotAuthorizedException("");

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        ArrayList<User> userList = new ArrayList<User>();

        try {
            if (id == -1) {
                userList = (ArrayList<User>) getAllUsers(loggedInUser);
            }
            else
            {
                stmt = con.prepareStatement(
                        "select a.id id, a.clientId clientid,h.name clientname, a.firstname firstname, a.lastname lastname,a.isactive isActive, " +
                                " e.roleid roleid, f.name rolename, a.username username," +
                                " (b.address).addLine1 line1, (b.address).addLine2 line2, (b.address).addLine3 line3 ," +
                                " (b.address).city city, (b.address).state, (b.address).phone phones, b.designation ," +
                                " c.divid divId , b.empNumber, d.name divname,b.createDate cdate, b.createBy cby ," +
                                " b.updateDate  udate,  b.updateBy uby ,(select a1.username from master.users a1," +
                                " client1.userprofile b1 where a1.id = b1.updateby and b1.userId = a.id) updatename " +
                                " from master.users a left join " +
                                loggedInUser.schemaName + ".userprofile b on a.id = b.userid " +
                                " left join " + loggedInUser.schemaName + ".userdivmap c on a.id = c.userid left join " +
                                loggedInUser.schemaName + ".divisions d on d.id = c.divid left join master.userrolemap e on " +
                                " e.userid = a.id left join master.roles f on f.id = e.roleid  " +
                                " left join master.clients h on h.id = a.clientId " +
                                " where c.divid = ? order by a.id asc ");
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
                    user.createDate = result.getDate("cdate");
                    user.createBy = result.getInt("cby");
                    user.updateDate = result.getDate("cdate");
                    user.updateBy = result.getInt("cby");
                    user.designation = result.getString("designation");
                    user.updatedName = result.getString("updatename");
                    userList.add(user);
                }
            }
        }finally {
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

        if (!Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole)))
            throw new NotAuthorizedException("");


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
    }


    /**
     * Method allows to update user Details
     *
     * @param node
     * @param loggedInUser
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws RequiredDataMissing
     * @throws NamingException
     */
    public static void updateUser(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

        // TODO: check if the user has rights to perform this action.

        boolean isAllowed = false;

        for (Role role : loggedInUser.roles) {
            if (Permissions.isAuthorised(role.roleId, Permissions.USER_PROFILE, Permissions.READ_ONLY)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed)
            throw new NotAuthorizedException("");

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt;
        ResultSet result;
        int accessTimeout = 0;
        javax.naming.Context env = null;
        env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement("UPDATE master.users SET firstname = ?,lastname = ?,clientId= ?,username=?, isactive=? " +
                    " WHERE id = ?");
            stmt.setString(1, node.get("firstName").asText());
            stmt.setString(2, node.get("lastName").asText());
            stmt.setInt(3, node.get("clientId").asInt());
            stmt.setString(4, node.get("username").asText());
            stmt.setBoolean(5,node.get("isActive").asBoolean());
            stmt.setInt(6, node.get("userid").asInt());

            int affectedRows = stmt.executeUpdate();
            System.out.println(affectedRows);

            if (affectedRows == 0)
                throw new SQLException("Update user failed.");
            else {

                // operation.
                String[] phoneArr = new String[node.withArray("phones").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("phones").size(); i++) {
                    phoneArr[i] = node.withArray("phones").get(i).asText();
                }

                Array pharr = con.createArrayOf("text", phoneArr);


                stmt = con.prepareStatement("UPDATE " + loggedInUser.schemaName + ".userProfile SET"
                        + " address = ROW(?,?,?,?,?,?), designation = ?, empNumber = ?, updateDate= ?, updateBy= ? where userid = ?");

                System.out.println(stmt.toString());

                stmt.setString(1, node.get("addLine1").asText());
                stmt.setString(2, node.get("addLine2").asText());
                stmt.setString(3, node.get("addLine3").asText());
                stmt.setString(4, node.get("city").asText());
                stmt.setString(5, node.get("state").asText());
                stmt.setArray(6, pharr);
                stmt.setString(7, node.get("designation").asText());
                stmt.setString(8, node.get("empNumber").asText());
                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                stmt.setInt(10, loggedInUser.id);
                stmt.setInt(11, node.get("userid").asInt());

                stmt.executeUpdate();

                stmt = con.prepareStatement("UPDATE " + loggedInUser.schemaName + ".userdivmap SET divid=? where userid=?");
                stmt.setInt(1, node.get("divid").asInt());
                stmt.setInt(2, node.get("userid").asInt());
                stmt.executeUpdate();

                stmt = con.prepareStatement("SELECT roleid from master.userrolemap where userid=?");
                stmt.setInt(1, node.get("userid").asInt());
                result = stmt.executeQuery();

                stmt = con.prepareStatement("UPDATE master.userrolemap SET roleid=? where userid=?");

                if (result.next()) {
                    stmt.setInt(1, node.get("roleid").asInt());
                    stmt.setInt(2, node.get("userid").asInt());
                    System.out.println("NEW ID : " + node.get("roleid").asInt());
                    if (result.getInt("roleid") != node.get("roleid").asInt()) {
                        System.out.println("OLD ID : " + result.getInt("roleid"));
                        stmt.executeUpdate();

                        if (node.get("isPublic").asBoolean()) {

                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_PUBLIC_TIMEOUT");
                            Mem.setData(node.get("username").asInt() + "#ROLECHANGED", accessTimeout * 2);

                        } else {
                            accessTimeout = (int) env.lookup("ACCESS_TOKEN_WORK_TIMEOUT");
                            Mem.setData(node.get("username").asInt() + "#ROLECHANGED", accessTimeout * 2);

                        }

                        stmt = con.prepareStatement("insert into master.userrolemaphistory (userid, roleid, effectdate, createdate, createby) values (?,?,?,?,?)");
                        stmt.setInt(1, node.get("userid").asInt());
                        stmt.setInt(2, node.get("roleid").asInt());
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);

                        int affectedrows = stmt.executeUpdate();
                        System.out.println(affectedrows);

                        throw new Exception(new NotAuthorizedException("You Must Login Again..."));
                    }
                }

            }

            con.commit();
        } catch (Exception ex) {
            if (con != null)
                con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(false);
            if (con != null)
                con.close();
        }

    }

    /***
     *  Method used to give all Clients which are active
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<User> getAllClients(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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
                    System.out.print(result);
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
     * Method used to send mail to user.
     *
     * @param username
     * @param from
     * @param password
     * @param emailBody
     * @return
     * @throws MessagingException
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static boolean generateAndSendEmail(String username,String from, String password,String emailBody) throws MessagingException, SQLException, NamingException, ClassNotFoundException {
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
        generateMailMessage.setContent(emailBody, "text/html");
        System.out.println("Mail Session has been created successfully..");

        // Step3
//        System.out.println("\n\n 3rd ===> Get Session and Send mail");
        Transport transport = getMailSession.getTransport("smtp");

        transport.connect("smtp.gmail.com", from , password);
        if (transport.isConnected())
        {
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();
            return true;
        }
        else
        {
            return false;
        }
    }
}


