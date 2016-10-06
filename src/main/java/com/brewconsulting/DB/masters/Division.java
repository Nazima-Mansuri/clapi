package com.brewconsulting.DB.masters;

import java.nio.file.AccessDeniedException;
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

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
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

    public final static String dateFormat = "dd-MM-YYYY hh:mm:ss";
    public static DateFormat df;

    public static Date stringToDate(String dateAsString) {
        try {
            df = new SimpleDateFormat(dateFormat);
            return df.parse(dateAsString);
        } catch (ParseException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static List<Division> getAllDivisions(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<Division> divisions = new ArrayList<Division>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("select d.id, d.name, d.description, d.createDate, d.createBy,d.updateDate,d.updateBy,u.username,(address).addLine1 addLine1,(address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city,(address).state state,(address).phone phones from "
                                    + schemaName
                                    + ".divisions d left join master.users u on d.updateBy = u.id left join "
                                    + schemaName
                                    + ".userprofile p on d.updateby = p.userid ORDER BY d.id DESC");
                    result = stmt.executeQuery();
                    System.out.print(result);
                    while (result.next()) {
                        Division div = new Division();
                        div.id = result.getInt(1);
                        div.name = result.getString(2);
                        div.description = result.getString(3);
                        div.createDate = result.getTimestamp(4);
                        div.createBy = result.getInt(5);
                        div.updateDate = result.getTimestamp(6);
                        div.updateBy = result.getInt(7);
                        div.username = result.getString(8);
                        div.addLine1 = result.getString(9);
                        div.addLine2 = result.getString(10);
                        div.addLine3 = result.getString(11);
                        div.city = result.getString(12);
                        div.state = result.getString(13);
                        if (result.getArray(14) != null)
                            div.phones = (String[]) result.getArray(14)
                                    .getArray();
                        div.firstname = loggedInUser.firstName;
                        div.lastname = loggedInUser.lastName;

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

    public static Division getDivisionById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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
                                    + " d.updateBy,(address).addLine1 addLine1,(address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city,(address).state state,(address).phone phones from "
                                    + schemaName
                                    + ".divisions d left join "
                                    + schemaName
                                    + ".userprofile p on d.updateby = p.userid where id = ?");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        division = new Division();
                        division.id = result.getInt(1);
                        division.name = result.getString(2);
                        division.description = result.getString(3);
                        division.createDate = result.getTimestamp(4);
                        division.createBy = result.getInt(5);
                        division.updateDate = result.getTimestamp(6);
                        division.updateBy = result.getInt(7);
                        division.addLine1 = result.getString(8);
                        division.addLine2 = result.getString(9);
                        division.addLine3 = result.getString(10);
                        division.city = result.getString(11);
                        division.state = result.getString(12);
                        if (result.getArray(13) != null)
                            division.phones = (String[]) result.getArray(13)
                                    .getArray();
                        division.firstname = loggedInUser.firstName;
                        division.lastname = loggedInUser.lastName;
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

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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
                stmt.setString(1, node.get("name").asText());

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

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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
                    stmt.setString(1, node.get("name").asText());

                    // It checks if Description is given or not
                    if (node.has("description"))
                        stmt.setString(2, node.get("description").asText());
                    stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                    stmt.setInt(4, loggedInUser.id);
                    stmt.setInt(5, node.get("divisionId").asInt());

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

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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