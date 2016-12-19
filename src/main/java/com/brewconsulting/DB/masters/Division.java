package com.brewconsulting.DB.masters;

import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.NoContentException;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.exceptions.NoDataFound;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Division {

    @JsonProperty("id")
    public int id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateDate;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("username")
    public String username;

    @JsonProperty("Firstname")
    public String firstname;

    @JsonProperty("Lastname")
    public String lastname;

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

    // make the default constructor visible to package only.
    Division() {

    }

    public static final int DIVISION = 2;


    /***
     * Method used to get all Divisions.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Division> getAllDivisions(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;
        String roleName = loggedInUser.roles.get(0).roleName;
        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        ArrayList<Division> divisions = new ArrayList<Division>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        ResultSet result1 = null;
        String name = null;
        int divId = 0;
        List<Integer> idList = new ArrayList<>();
        try {
            stmt = con.prepareStatement("SELECT name from master.roles where id = ? ");
            stmt.setInt(1, 2);
            result1 = stmt.executeQuery();
            while (result1.next()) {
                name = result1.getString(1);
            }
            if (roleName.equals(name)) {
                stmt = con.prepareStatement("SELECT divid from " + schemaName + ".userdivmap where userid = ? ");
                stmt.setInt(1, loggedInUser.id);
                result1 = stmt.executeQuery();
                while (result1.next()) {
                    idList.add(result1.getInt(1));
                }
                try {
                    if (con != null) {

                        for (int i = 0; i < idList.size(); i++) {
                            stmt = con
                                    .prepareStatement("select d.id, d.name, d.description, d.createDate, d.createBy,d.updateDate,d.updateBy," +
                                            " u.username,u.firstname,u.lastname,(address).addLine1 addLine1," +
                                            " (address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city," +
                                            " (address).state state,(address).phone phones from "
                                            + schemaName
                                            + ".divisions d left join master.users u on d.updateBy = u.id left join "
                                            + schemaName
                                            + ".userprofile p on d.updateby = p.userid " +
                                            " WHERE d.id = ? " +
                                            " ORDER BY d.updateDate DESC");
                            stmt.setInt(1, idList.get(i));
                            result = stmt.executeQuery();
                            while (result.next()) {
                                Division div = new Division();
                                div.id = result.getInt(1);
                                div.name = result.getString(2);
                                div.description = result.getString(3);
                                div.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(4).getTime())));
                                div.createBy = result.getInt(5);
                                div.updateDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                                div.updateBy = result.getInt(7);
                                div.username = result.getString(8);
                                div.firstname = result.getString(9);
                                div.lastname = result.getString(10);
                                div.addLine1 = result.getString(11);
                                div.addLine2 = result.getString(12);
                                div.addLine3 = result.getString(13);
                                div.city = result.getString(14);
                                div.state = result.getString(15);
                                if (result.getArray(16) != null)
                                    div.phones = (String[]) result.getArray(16)
                                            .getArray();

                                divisions.add(div);
                            }
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
                return divisions;

            } else {
                if (Permissions.isAuthorised(userRole, DIVISION).equals("Read") ||
                        Permissions.isAuthorised(userRole, DIVISION).equals("Write")) {
                    try {
                        if (con != null) {
                            stmt = con
                                    .prepareStatement("select d.id, d.name, d.description, d.createDate, d.createBy,d.updateDate,d.updateBy," +
                                            " u.username,u.firstname,u.lastname,(address).addLine1 addLine1," +
                                            " (address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city," +
                                            " (address).state state,(address).phone phones from "
                                            + schemaName
                                            + ".divisions d left join master.users u on d.updateBy = u.id left join "
                                            + schemaName
                                            + ".userprofile p on d.updateby = p.userid ORDER BY d.updateDate DESC");
                            result = stmt.executeQuery();
                            while (result.next()) {
                                Division div = new Division();
                                div.id = result.getInt(1);
                                div.name = result.getString(2);
                                div.description = result.getString(3);
                                div.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(4).getTime())));
                                div.createBy = result.getInt(5);
                                div.updateDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                                div.updateBy = result.getInt(7);
                                div.username = result.getString(8);
                                div.firstname = result.getString(9);
                                div.lastname = result.getString(10);
                                div.addLine1 = result.getString(11);
                                div.addLine2 = result.getString(12);
                                div.addLine3 = result.getString(13);
                                div.city = result.getString(14);
                                div.state = result.getString(15);
                                if (result.getArray(16) != null)
                                    div.phones = (String[]) result.getArray(16)
                                            .getArray();

                                divisions.add(div);
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
                    return divisions;
                } else {
                    throw new NotAuthorizedException("");
                }
            }
        } finally {
            if (result1 != null)
                if (result1.isClosed())
                    result1.close();
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
    }

    /***
     * Method used to get Particular Division
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Division getDivisionById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, DIVISION).equals("Read") ||
                Permissions.isAuthorised(userRole, DIVISION).equals("Write")) {

            Division division = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("select d.id, d.name, d.description, d.createDate, d.createBy, d.updateDate, "
                                    + " d.updateBy,u.username,u.firstname,u.lastname,(address).addLine1 addLine1,(address).addLine2 addLine2," +
                                    " (address).addLine3 addLine3,(address).city city,(address).state state,(address).phone phones from "
                                    + schemaName
                                    + ".divisions d " +
                                    " left join master.users u on u.id = d.updateby " +
                                    " left join " + schemaName + ".userprofile p on d.updateby = p.userid " +
                                    "where id = ?");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        division = new Division();
                        division.id = result.getInt(1);
                        division.name = result.getString(2);
                        division.description = result.getString(3);
                        division.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(4).getTime())));
                        division.createBy = result.getInt(5);
                        division.updateDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        division.updateBy = result.getInt(7);
                        division.username = result.getString(8);
                        division.firstname = result.getString(9);
                        division.lastname = result.getString(10);
                        division.addLine1 = result.getString(11);
                        division.addLine2 = result.getString(12);
                        division.addLine3 = result.getString(13);
                        division.city = result.getString(14);
                        division.state = result.getString(15);
                        if (result.getArray(16) != null)
                            division.phones = (String[]) result.getArray(16)
                                    .getArray();
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
            return division;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method allows user to insert Division in Database.
     *
     * @param loggedInUser
     * @param node
     * @return
     * @throws Exception
     */
    public static int addDivision(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, DIVISION).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".divisions(name,description,createDate,createBy,updateDate,"
                                        + "updateBy) values (?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                if (node.has("name"))
                    stmt.setString(1, node.get("name").asText());
                else
                    throw new Exception("Division Name is not defined.");

                // It checks if Description is given or not
                if (node.has("description"))
                    stmt.setString(2, node.get("description").asText());
                else
                    stmt.setString(2, null);

                stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                stmt.setInt(4, loggedInUser.id);
                stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                stmt.setInt(6, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Division Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int divisionId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    divisionId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return divisionId;

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
     * Method allows user to Update Division in Database.
     *
     * @param loggedInUser
     * @param node
     * @return
     * @throws Exception
     */
    public static int updateDivision(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, DIVISION).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;

            try {
                // It checks if connection is not null then perform update
                // operation.
                if (con != null) {
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".divisions SET name = ?,description = ?,updateDate = ?,"
                                    + "updateBy = ? WHERE id = ?");
                    if (node.has("name"))
                        stmt.setString(1, node.get("name").asText());
                    else
                        throw new Exception("Division Name is Not Defined.");

                    // It checks if Description is given or not
                    if (node.has("description"))
                        stmt.setString(2, node.get("description").asText());
                    stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                    stmt.setInt(4, loggedInUser.id);

                    if (node.has("divisionId"))
                        stmt.setInt(5, node.get("divisionId").asInt());
                    else
                        throw new Exception("Division ID is not Defined for update Division.");

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
     * Method allows user to Delete Division from Database.
     *
     * @param loggedInUser
     * @param id
     * @throws Exception
     * @Return
     */

    public static int deleteDivision(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, DIVISION).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".divisions WHERE id = ?");

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
}