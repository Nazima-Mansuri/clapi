package com.brewconsulting.DB.masters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.NotAuthorizedException;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Territory {
    @JsonProperty("id")
    public int id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("addLine1")
    public String addLine1;

    @JsonProperty("addLine2")
    public String addLine2;

    @JsonProperty("addLine3")
    public String addLine3;

    @JsonProperty("city")
    public String city;

    @JsonProperty("state")
    public String state;

    @JsonProperty("phones")
    public String[] phones;

    @JsonProperty("parentId")
    public int parentId;

    @JsonProperty("personId")
    public int personId;

    @JsonProperty("divId")
    public int divId;

    @JsonProperty("username")
    public String username;

    @JsonProperty("firstname")
    public String firstname;

    @JsonProperty("lastname")
    public String lastname;

    @JsonProperty("isHistory")
    public boolean isHistory;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updatedate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updatedate;

    @JsonProperty("updateby")
    public int updateby;
    public static final int Territory = 3;

    // make the default constructor visible to package only.
    public Territory() {

    }

    /**
     * @param loggedInUser
     * @return
     * @throws Exception
     * @deprecated use getAllTerritories(LoggedInUser, Division Id)
     */
    @Deprecated
    public static List<Territory> getAllTerritories(LoggedInUser loggedInUser) throws Exception {
        throw new Exception("Please use the function with div id in it. ");
    }

    /***
     * Method allows user to get All Details of Territorie.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<terrWrapper> getAllTerritories(LoggedInUser loggedInUser, int divId) throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Read") ||
                Permissions.isAuthorised(userRole, Territory).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
//		ArrayList<Territory> territories = new ArrayList<Territory>();
            ArrayList<terrWrapper> territories = new ArrayList<terrWrapper>();
//		Map<Integer, Territory> lookup = new HashMap<Integer, Territory>();
            Map<Integer, terrWrapper> lookup = new HashMap<Integer, terrWrapper>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                if (con != null) {

                    stmt = con.prepareStatement(
                            "select t.id,name,(address).addLine1 addLine1, " + "(address).addLine2 addLine2,"
                                    + "(address).addLine3 addLine3,(address).city city,(address).state state,"
                                    + "(address).phone phones,parentId,divId,u.userid,uf.username from " +
                                    "(select * from " + schemaName + ".territories t where divid = ?)t" +
                                    " left join " + schemaName + ".userterritorymap u on t.id = u.terrid");
                    stmt.setInt(1, divId);

                    result = stmt.executeQuery();

                    while (result.next()) {

                        Territory terr = new Territory();
                        terrWrapper tw = new terrWrapper();

                        terr.id = result.getInt(1);
                        terr.name = result.getString(2);
                        terr.addLine1 = result.getString(3);
                        terr.addLine2 = result.getString(4);
                        terr.addLine3 = result.getString(5);
                        terr.city = result.getString(6);
                        terr.state = result.getString(7);
                        // If phone number is null then it gives null pointer
                        // exception here.
                        // So it check that the phone number is null or not
                        if (result.getArray(8) != null)
                            terr.phones = (String[]) result.getArray(8).getArray();

                        terr.parentId = result.getInt(9);
                        terr.divId = result.getInt(10);
                        terr.personId = result.getInt(11);
                        terr.username = result.getString(12);
                        tw.text = terr.name;
                        tw.data = terr;
//                        tw.children = terr.children;
//					lookup.put(terr.id, terr);
                        lookup.put(terr.id, tw);
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


            for (terrWrapper territory : lookup.values()) {
                if (territory.data.parentId == 0) {
                    territories.add(territory);
                    System.out.println("territory" + territory);
                } else if (lookup.containsKey(territory.data.parentId))
                    lookup.get(territory.data.parentId).children.add(territory);
            }
            System.out.println("territory" + territories);
            return territories;
        } else {
            throw new NotAuthorizedException("");

        }
    }

    /***
     * Method allows user to get Details of Particular Territorie.
     *
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static Territory getTerritorieById(int id, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Read") ||
                Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            Territory territory = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(
                            "select id, name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
                                    + "(address).addLine3 addLine3,(address).city city,(address).state state,"
                                    + "(address).phone phones," + "parentId,divId from " + schemaName
                                    + ".territories where id = ?");

                    stmt.setInt(1, id);

                    result = stmt.executeQuery();
                    if (result.next()) {
                        territory = new Territory();
                        terrWrapper tw = new terrWrapper();
                        territory.id = result.getInt(1);
                        territory.name = result.getString(2);
                        territory.addLine1 = result.getString(3);
                        territory.addLine2 = result.getString(4);
                        territory.addLine3 = result.getString(5);
                        territory.city = result.getString(6);
                        territory.state = result.getString(7);
                        // If phone number is null then it gives null pointer
                        // exception here.
                        // So it check that the phone number is null or not
                        if (result.getArray(8) != null)
                            territory.phones = (String[]) result.getArray(8).getArray();
                        territory.parentId = result.getInt(9);
                        territory.divId = result.getInt(10);


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


            return territory;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Get all Territories of specific Division.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<terrWrapper> getTerritorieByDivisionId(int id, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Read") ||
                Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            Territory territory = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            ArrayList<terrWrapper> territories = new ArrayList<terrWrapper>();
            Map<Integer, terrWrapper> lookup = new HashMap<Integer, terrWrapper>();
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {

                    stmt = con
                            .prepareStatement("select distinct t.id,CASE WHEN b.terrid IS NULL THEN 'false' ELSE 'true' END AS isHistory," +
                                    " name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
                                    + "(address).addLine3 addLine3,(address).city city,(address).state state,"
                                    + "(address).phone phones,parentId,divId,u.userid,uf.username,uf.firstname,uf.lastname,"
                                    + " t.createdate,t.createby,t.updatedate,t.updateby from " +
                                    " (select * from client1.territories t WHERE divid = ?)t" +
                                    " left join " + schemaName + ".userterritorymap u " + "on t.id=u.terrid " +
                                    " LEFT JOIN " + schemaName + ".userterritorymaphistory b ON t.id = b.terrid " +
                                    " left join master.users uf on uf.id = u.userid" +
                                    " ORDER BY t.updatedate DESC");
                    stmt.setInt(1, id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        territory = new Territory();
                        terrWrapper tw = new terrWrapper();
                        territory.id = result.getInt(1);
                        territory.isHistory = result.getBoolean(2);
                        territory.name = result.getString(3);
                        territory.addLine1 = result.getString(4);
                        territory.addLine2 = result.getString(5);
                        territory.addLine3 = result.getString(6);
                        territory.city = result.getString(7);
                        territory.state = result.getString(8);
                        // If phone number is null then it gives null pointer
                        // exception here.
                        // So it check that the phone number is null or not
                        if (result.getArray(9) != null)
                            territory.phones = (String[]) result.getArray(9).getArray();
                        territory.parentId = result.getInt(10);
                        territory.divId = result.getInt(11);
                        territory.personId = result.getInt(12);
                        territory.username = result.getString(13);
                        territory.firstname = result.getString(14);
                        territory.lastname = result.getString(15);
                        territory.createDate = result.getTimestamp(16);
                        territory.createBy = result.getInt(17);
                        territory.updatedate = result.getTimestamp(18);
                        territory.updateby = result.getInt(19);
                        if (territory.firstname != null && territory.lastname != null)
                            tw.text = territory.name + " - " + territory.firstname + " " + territory.lastname;
                        else
                            tw.text = territory.name;
                        tw.data = territory;
//					lookup.put(terr.id, terr);
                        lookup.put(territory.id, tw);

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


            for (terrWrapper terr : lookup.values()) {
                if (terr.data.parentId == 0)
                    territories.add(terr);
                else if (lookup.containsKey(terr.data.parentId))
                    lookup.get(terr.data.parentId).children.add(terr);
            }

            for (terrWrapper terr : lookup.values()) {
                if (terr.children.size() > 0) {
                    Collections.sort(terr.children, new Comparator<terrWrapper>() {
                        @Override
                        public int compare(terrWrapper t1, terrWrapper t2) {
                            return (t1.data.name.toLowerCase().compareTo(t2.data.name.toLowerCase()));
                        }
                    });
                }
            }

            Collections.sort(territories, new Comparator<terrWrapper>() {

                @Override
                public int compare(terrWrapper t1, terrWrapper t2) {
                    return (t1.data.name.toLowerCase().compareTo(t2.data.name.toLowerCase()));
                }
            });

            return territories;
        } else {
            throw new NotAuthorizedException("");

        }
    }

    /***
     * Method allows user to insert Territorie in Database.
     *
     * @param loggedInUser
     * @param node
     * @return
     * @throws Exception
     */
    public static int addTerritory(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow;
            ResultSet result = null;
            try {
                con.setAutoCommit(false);

                // TODO: set up the phones string array
                String[] phoneArr = new String[node.withArray("phones").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("phones").size(); i++) {
                    phoneArr[i] = node.withArray("phones").get(i).asText();
                }

                Array pharr = con.createArrayOf("text", phoneArr);

                stmt = con.prepareStatement(
                        "INSERT INTO " + schemaName
                                + ".territories(name,parentid,address,divid,createdate,createby,updatedate,updateby) " +
                                " values (?,?,ROW(?,?,?,?,?,?),?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, node.get("name").asText());
                if (node.has("parentId"))
                    stmt.setInt(2, node.get("parentId").asInt());
                else
                    stmt.setInt(2, 0);

                // It checks that the address1 has value or not
                if (node.has("addLine1"))
                    stmt.setString(3, node.get("addLine1").asText());
                else
                    stmt.setString(3, null);
                // It checks that the address2 has value or not
                if (node.has("addLine2"))
                    stmt.setString(4, node.get("addLine2").asText());
                else
                    stmt.setString(4, null);
                // It checks that the address3 has value or not
                if (node.has("addLine3"))
                    stmt.setString(5, node.get("addLine3").asText());
                else
                    stmt.setString(5, null);

                // It checks that the city has value or not
                if (node.has("city"))
                    stmt.setString(6, node.get("city").asText());
                else
                    stmt.setString(6, null);

                // It checks that the state has value or not
                if (node.has("state"))
                    stmt.setString(7, node.get("state").asText());
                else
                    stmt.setString(7, null);

                stmt.setArray(8, pharr);

                stmt.setInt(9, node.get("divId").asInt());

                stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
                stmt.setInt(11, loggedInUser.id);
                stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));
                stmt.setInt(13, loggedInUser.id);

                affectedRow = stmt.executeUpdate();

                if (affectedRow == 0)
                    throw new SQLException("Add Territorie Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int territoryId;
                if (generatedKeys.next())
                    // It gives last inserted Id from territory
                    territoryId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                if (node.get("personId").asInt() != 0) {
                    stmt = con.prepareStatement(
                            "SELECT userId from " + schemaName + ".userterritorymap WHERE userId = ?");

                    stmt.setInt(1, node.get("personId").asInt());
                    result = stmt.executeQuery();

                    if (!result.next()) {
                        System.out.println("In If Method..");
                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".userterritorymap"
                                + "(userId,terrId,effectDate,createBy,createDate) values (?,?,?,?,?)");
                        stmt.setInt(1, node.get("personId").asInt());
                        stmt.setInt(2, territoryId);
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setInt(4, loggedInUser.id);
                        stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));

                        stmt.executeUpdate();

                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".userterritorymaphistory"
                                + "(userId,terrId,effectDate,endDate,createBy,createDate) values (?,?,?,?,?,?)");

                        stmt.setInt(1, node.get("personId").asInt());
                        stmt.setInt(2, territoryId);
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                        stmt.executeUpdate();
                    } else {
                        System.out.println("In Else Method..");
                    }
                }

                con.commit();
                return territoryId;

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
     * Method allows user to Update Territorie in Database. Then insert data in
     * userTerritoryMap and userTerritoryMapHistory
     *
     * @param loggedInUser
     * @param node
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
    public static int updateTerritory(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow;
            ResultSet result;

            try {
                con.setAutoCommit(false);
                // It checks if connection is not null then perform update
                // operation.
                String[] phoneArr = new String[node.withArray("phones").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("phones").size(); i++) {
                    phoneArr[i] = node.withArray("phones").get(i).asText();
                }

                Array pharr = con.createArrayOf("text", phoneArr);

                stmt = con.prepareStatement(
                        "UPDATE " + schemaName +
                                ".territories SET name = ?,address =ROW(?,?,?,?,?,?), updatedate = ?,updateby = ? " +
                                "  WHERE id = ?");
                stmt.setString(1, node.get("name").asText());
                stmt.setString(2, node.get("addLine1").asText());
                stmt.setString(3, node.get("addLine2").asText());
                stmt.setString(4, node.get("addLine3").asText());
                stmt.setString(5, node.get("city").asText());
                stmt.setString(6, node.get("state").asText());
                stmt.setArray(7, pharr);
                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                stmt.setInt(9, loggedInUser.id);
                stmt.setInt(10, node.get("id").asInt());

                affectedRow = stmt.executeUpdate();

                if (node.get("personId").asInt() > 0) {
                    stmt = con.prepareStatement(
                            "SELECT userId from " + schemaName + ".userterritorymap WHERE userId = ?");
                    stmt.setInt(1, node.get("personId").asInt());
                    result = stmt.executeQuery();

                    System.out.println();
                    if (!result.next()) {
                        System.out.println("If Method ");
                        // It Insert data in userTerritoryMap with new userId
                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".userterritorymap"
                                + "(userId,terrId,effectDate,createBy,createDate) values (?,?,?,?,?)");

                        stmt.setInt(1, node.get("personId").asInt());
                        stmt.setInt(2, node.get("id").asInt());
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setInt(4, loggedInUser.id);
                        stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));

                        stmt.executeUpdate();

                        // It Insert data in userTerritoryMapHistory with new userId
                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".userterritorymaphistory"
                                + "(userId,terrId,effectDate,endDate,createBy,createDate) values (?,?,?,?,?,?)");

                        stmt.setInt(1, node.get("personId").asInt());
                        stmt.setInt(2, node.get("id").asInt());
                        stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                        stmt.executeUpdate();
                    }

                }
                con.commit();
                return affectedRow;
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
     * Method allows user to Delete Territorie from Database.
     *
     * @param loggedInUser
     * @param id
     * @throws Exception
     * @Return
     */

    public static int deleteTerritory(int id, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName + ".territories WHERE id = ?");

                    stmt.setInt(1, id);
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
        } else {
            throw new NotAuthorizedException("");

        }
    }

    /***
     * Method allows user to Delete person from userMapTerritorie and update
     * endDate in userTerritoryMapHistory Tables from Database.
     *
     * @param loggedInUser
     * @param node
     * @throws Exception
     * @Return
     */
    @SuppressWarnings("resource")
    public static int deassociateUser(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Territory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result;
            int userId = 0;
            int affectedRow;

            try {
                if (con != null) {
                    // It gets userId from userterritorymap table
                    stmt = con.prepareStatement(
                            "SELECT userId from " + schemaName + ".userterritorymap " + "where terrId = ?");
                    stmt.setInt(1, node.get("id").asInt());
                    result = stmt.executeQuery();
                    if (result.next())
                        userId = result.getInt(1);

                    // It delete entry of deassociate person from
                    // userTerritoryMap
                    // table .
                    stmt = con.prepareStatement(
                            "DELETE from " + schemaName + ".userterritorymap " + "where userId = ? AND terrId = ?");
                    stmt.setInt(1, userId);
                    stmt.setInt(2, node.get("id").asInt());
                    affectedRow = stmt.executeUpdate();

                    // It update endDate of deassociate person in
                    // userTerritoryMapHistory table .
                    stmt = con.prepareStatement("UPDATE " + schemaName
                            + ".userterritorymaphistory SET endDate = ? WHERE userId = ? AND terrId = ?");
                    stmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
                    stmt.setInt(2, userId);
                    stmt.setInt(3, node.get("id").asInt());
                    stmt.executeUpdate();

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
            return affectedRow;
        } else {
            throw new NotAuthorizedException("");
        }
    }

}

class terrWrapper {
    public String text;
    public Territory data;
    public ArrayList<terrWrapper> children = new ArrayList<>();
}