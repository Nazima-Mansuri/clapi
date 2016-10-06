
package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.exceptions.RequiredDataMissing;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;

public class User {

    @JsonProperty("clientId")
    @JsonView(UserViews.authView.class)
    public int clientId; // -1 means no client

    @JsonProperty("id")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class})
    public int id;

    @JsonProperty("username")
    @JsonView({UserViews.bareView.class, UserViews.deAssociateView.class})
    public String username;

    @JsonView(UserViews.authView.class)
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

    @JsonView(UserViews.profileView.class)
    @JsonProperty("isActive")
    public boolean isActive;


    // make the constructor private.
    protected User() {

    }

    public static User authenticate(String username, String password)
            throws ClassNotFoundException, SQLException, NamingException {
        User user = null;
        Connection con = DBConnectionProvider.getConn();
        try {

            PreparedStatement stmt = con.prepareStatement(
                    "select a.id, a.clientId, a.firstName, a.lastName, schemaName, d.id roleid, d.name rolename, a.username "
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
//		if (!isUserAdmin(user))
//			fillProfileInfo(user);

        return user;
    }

    private static User fillFullProfile(LoggedInUser user, int id)
            throws ClassNotFoundException, SQLException, NamingException {
        Connection con = DBConnectionProvider.getConn();
        ResultSet schemaUsers = null;
        PreparedStatement stmt = null;
        User userProfile = null;
        try {
            stmt = con.prepareStatement(
                    "select a.id id, a.clientId clientid, a.firstname firstname, a.lastname lastname, schemaName schemaname,"
                            + "c.id roleid, c.name rolename, a.username username, "
                            + "(d.address).addLine1 line1, (d.address).addLine2 line2, (d.address).addLine3 line3,"
                            + "(d.address).city city, (d.address).state, (d.address).phone phones, d.roleid, d.divId divid,"
                            + "empNumber, e.name divname," + "d.createDate cdate, d.createBy cby, "
                            + "d.updateDate  udate,  d.updateBy uby"
                            + " from master.users a, master.userRoleMap b, master.roles c, " + user.schemaName
                            + ".userProfile d, " + user.schemaName + ".divisions e, master.clients f "
                            + " where a.isActive and a.id = ? and a.id = b.userId and b.roleId = c.id"
                            + " and d.userId = a.id and d.divId = e.Id and f.id = a.clientId");
            stmt.setInt(1, id);
            schemaUsers = stmt.executeQuery();
            while (schemaUsers.next()) {
                if (userProfile == null) {
                    userProfile = new User();
                    userProfile.id = schemaUsers.getInt("id");
                    userProfile.clientId = schemaUsers.getInt("clientid");
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
                    userProfile.divId = schemaUsers.getInt("divid");
                    userProfile.divName = schemaUsers.getString("divname");
                    userProfile.empNum = schemaUsers.getString("empnumber");
                    userProfile.createDate = schemaUsers.getDate("cdate");
                    userProfile.createBy = schemaUsers.getInt("cby");
                    userProfile.updateDate = schemaUsers.getDate("cdate");
                    userProfile.updateBy = schemaUsers.getInt("cby");
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

    private static void fillProfileInfo(User user) throws ClassNotFoundException, SQLException, NamingException {
        Connection con = DBConnectionProvider.getConn();
        ResultSet schemaUsers = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(
                    "select (address).addLine1 line1, (address).addLine2 line2, (address).addLine3 line3, "
                            + "(address).city city, (address).state, (address).phone phones, roleid, a.divId divid, "
                            + "empNumber, b.name divname," + "a.createDate cdate, a.createBy cby, "
                            + "a.updateDate  udate,  a.updateBy uby from " + user.schemaName + ".userProfile a, "
                            + user.schemaName + ".divisions b where userId = ? and " + "a.divId = b.Id");
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
                user.roleid = schemaUsers.getInt("roleid");
                user.divId = schemaUsers.getInt("divid");
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

    public static int createUser(JsonNode node, LoggedInUser loggedInUser)
            throws ClassNotFoundException, SQLException, RequiredDataMissing, NamingException {

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

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement("insert into master.users (firstname, lastname, "
                    + "clientId, username, password,isactive) values (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, node.get("firstName").asText());

            stmt.setString(2, node.get("lastName").asText());

            stmt.setInt(3, node.get("clientId").asInt());

            stmt.setString(4, node.get("username").asText());

            stmt.setString(5, node.get("password").asText());

            if (node.has("isActive"))
                stmt.setBoolean(6, node.get("isActive").asBoolean());
            else
                stmt.setBoolean(6, true);

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
                    + "address, roleid, divId, empNumber, createBy, updateDate, updateBy) values (?, "
                    + "ROW(?,?,?,?,?,?), ?, ?, ?, ?,?,?)");
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
            stmt.setArray(7, pharr);
            stmt.setInt(8, node.get("roleid").asInt());
            stmt.setInt(9, node.get("divId").asInt());
            stmt.setString(10, node.get("empNumber").asText());
            stmt.setInt(11, loggedInUser.id);
            stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));
            stmt.setInt(13, loggedInUser.id);

            stmt.executeUpdate();

            // TODO: you need to check the return value here also and throw
            // error if insert failed.
            System.out.println("Out of if");

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
            con.commit();
            return userid;
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

    public static List<User> getDeassociateUser(LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (!Permissions.isAuthorised(userRole, Permissions.USER_PROFILE, Permissions.getAccessLevel(userRole)))
            throw new NotAuthorizedException("");

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        ArrayList<User> userList = new ArrayList<User>();

        try {

            stmt = con.prepareStatement("select a.userid id, (a.address).city city, c.username username " + "from "
                    + loggedInUser.schemaName + ".userProfile a left outer join " + loggedInUser.schemaName
                    + ".userTerritoryMap b " + "on (a.userId = b.userId), master.users c "
                    + "where b.terrId is null and c.id = a.userId");

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
     * WHY is this required?
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */

    public static List<User> getAllUsers(LoggedInUser loggedInUser)
            throws Exception {
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
                    "select a.id id, a.clientId clientid, a.firstname firstname, a.lastname lastname,a.isactive isActive,"
                            + "c.id roleid, c.name rolename, a.username username, "
                            + "(d.address).addLine1 line1, (d.address).addLine2 line2, (d.address).addLine3 line3,"
                            + "(d.address).city city, (d.address).state, (d.address).phone phones, d.roleid, d.divId divid,"
                            + "empNumber, e.name divname," + "d.createDate cdate, d.createBy cby, "
                            + "d.updateDate  udate,  d.updateBy uby"
                            + " from master.users a, master.userRoleMap b, master.roles c, "
                            + loggedInUser.schemaName
                            + ".userProfile d," + loggedInUser.schemaName + ".divisions e, master.clients f "
                            + " where a.id = b.userId and b.roleId = c.id"
                            + " and d.userId = a.id and d.divId = e.Id and f.id = a.clientId");
            result = stmt.executeQuery();
            while (result.next()) {

                User user = new User();
                user.id = result.getInt("id");
                user.clientId = result.getInt("clientid");
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
                user.divId = result.getInt("divid");
                user.divName = result.getString("divname");
                user.empNum = result.getString("empnumber");
                user.createDate = result.getDate("cdate");
                user.createBy = result.getInt("cby");
                user.updateDate = result.getDate("cdate");
                user.updateBy = result.getInt("cby");
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

        try {
            // If connection is not null then perform delete operation.
            if (con != null) {
                stmt = con.prepareStatement("UPDATE "
                        + "master.users SET isactive = ? WHERE id = ?");

                stmt.setBoolean(1, node.get("isactive").asBoolean());
                stmt.setInt(2, node.get("id").asInt());
                result = stmt.executeUpdate();
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
            throws ClassNotFoundException, SQLException, RequiredDataMissing, NamingException {

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

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement("UPDATE master.users SET firstname = ?,lastname = ?,clientId= ?,username=? WHERE id = ?");
            stmt.setString(1, node.get("firstName").asText());
            stmt.setString(2, node.get("lastName").asText());
            stmt.setInt(3, node.get("clientId").asInt());
            stmt.setString(4, node.get("username").asText());
            stmt.setInt(5, node.get("userid").asInt());

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
                        + " address = ROW(?,?,?,?,?,?), roleid = ?, divId = ?, empNumber = ?, updateDate= ?, updateBy= ? where userid = ?");

                System.out.println(stmt.toString());

                stmt.setString(1, node.get("addLine1").asText());
                stmt.setString(2, node.get("addLine2").asText());
                stmt.setString(3, node.get("addLine3").asText());
                stmt.setString(4, node.get("city").asText());
                stmt.setString(5, node.get("state").asText());
                stmt.setArray(6, pharr);
                stmt.setInt(7, node.get("roleid").asInt());
                stmt.setInt(8, node.get("divId").asInt());
                stmt.setString(9, node.get("empNumber").asText());
                stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
                stmt.setInt(11, loggedInUser.id);
                stmt.setInt(12, node.get("userid").asInt());

                stmt.executeUpdate();

                stmt = con.prepareStatement("SELECT roleid from master.userrolemap where userid=?");
                stmt.setInt(1, node.get("userid").asInt());
                result = stmt.executeQuery();

                stmt = con.prepareStatement("UPDATE master.userrolemap SET roleid=? where userid=?");

                if (result.next()) {
                    stmt.setInt(1, node.get("roleid").asInt());
                    stmt.setInt(2, node.get("userid").asInt());

                    if (result.getInt("roleid") != node.get("roleid").asInt()) {

                        stmt.executeUpdate();

                        stmt = con.prepareStatement("insert into master.userrolemaphistory (userid, roleid, effectdate, createdate, createby) values (?,?,?,?,?)");
                        stmt.setInt(1, node.get("userid").asInt());
                        stmt.setInt(2, node.get("roleid").asInt());
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);

                        int affectedrows = stmt.executeUpdate();
                        System.out.println(affectedrows);

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
}


