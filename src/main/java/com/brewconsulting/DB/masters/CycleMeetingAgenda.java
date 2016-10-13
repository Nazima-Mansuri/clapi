package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.DB.utils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.brewconsulting.DB.utils.stringToDate;


public class CycleMeetingAgenda {

    @JsonProperty("id")
    public int id;

    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonProperty("meetingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date meetingDate;

    @JsonProperty("sessionName")
    public String sessionName;

    @JsonProperty("sessionDesc")
    public String sessionDesc;

    @JsonProperty("sessionStartTime")
    public Time sessionStartTime;

    @JsonProperty("sessionEndTime")
    public Time sessionEndTime;

    @JsonProperty("createOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updatedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updatedOn;

    @JsonProperty("updateBy")
    public int updatedBy;


    // make the default constructor visible to package only.
    CycleMeetingAgenda() {

    }


    /**
     * method to get all cycle meeting agenda
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingAgenda> getAllCycleMeetingAgenda(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        String schemaName = loggedInUser.schemaName;

        Connection con = DBConnectionProvider.getConn();
        ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (con != null) {
                stmt = con
                        .prepareStatement("SELECT id, cyclemeetingid, meetingdate, sessionname, sessiondesc, sessionstarttime," +
                                "sessionendtime, createdon, createdby, updateon, updatedby" +
                                " FROM " + schemaName + ".cyclemeetingagenda");
                result = stmt.executeQuery();
                System.out.print(result);
                while (result.next()) {
                    CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                    cycleMeetingAgenda.id = result.getInt(1);
                    cycleMeetingAgenda.cycleMeetingId = result.getInt(2);
                    cycleMeetingAgenda.meetingDate = result.getDate(3);
                    cycleMeetingAgenda.sessionName = result.getString(4);
                    cycleMeetingAgenda.sessionDesc = result.getString(5);
                    cycleMeetingAgenda.sessionStartTime = result.getTime(6);
                    cycleMeetingAgenda.sessionEndTime = result.getTime(7);
                    cycleMeetingAgenda.createOn = result.getDate(8);
                    cycleMeetingAgenda.createBy = result.getInt(9);
                    cycleMeetingAgenda.updatedOn = result.getDate(10);
                    cycleMeetingAgenda.updatedBy = result.getInt(11);

                    cycleMeetingAgendas.add(cycleMeetingAgenda);
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
        return cycleMeetingAgendas;
    }


    /**
     * Method allow user to add cycle meeting agenda.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

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
                                    + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                    + "createdOn,createdBy,updateOn,updatedBy) values (?,?,?,?,?,?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, node.get("cycleMeetingId").asInt());
            stmt.setTimestamp(2, new Timestamp(stringToDate(node.get("meetingDate").asText()).getTime()));
            stmt.setString(3, node.get("sessionName").asText());
            stmt.setString(4, node.get("sessionDesc").asText());
            stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
            System.out.println(Time.valueOf(node.get("sessionStartTime").asText()));
            stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
            stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
            stmt.setInt(8, loggedInUser.id);
            stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
            stmt.setInt(10, loggedInUser.id);
            result = stmt.executeUpdate();

            if (result == 0)
                throw new SQLException("Add Division Failed.");

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            int id;
            if (generatedKeys.next())
                // It gives last inserted Id in divisionId
                id = generatedKeys.getInt(1);
            else
                throw new SQLException("No ID obtained");

            con.commit();
            return id;

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


    /**
     * Method allow user to update cycle meeting agenda.
     *
     * @param node
     * @param loggedInUser
     * @throws Exception
     */
    public static void updateCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {


        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;


        try {
            con.setAutoCommit(false);

            stmt = con
                    .prepareStatement(
                            "UPDATE "
                                    + schemaName
                                    + ".cycleMeetingAgenda SET cycleMeetingId =? ,meetingDate=?,sessionName=?,sessionDesc=?,sessionStartTime=?,sessionEndTime=?,"
                                    + "updateOn=?,updatedBy=? where id=?",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, node.get("cycleMeetingId").asInt());
            stmt.setTimestamp(2, new Timestamp(stringToDate(node.get("meetingDate").asText()).getTime()));
            stmt.setString(3, node.get("sessionName").asText());
            stmt.setString(4, node.get("sessionDesc").asText());
            stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
            stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
            stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
            stmt.setInt(8, loggedInUser.id);
            stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
            stmt.setInt(10, loggedInUser.id);
            stmt.setInt(11, node.get("id").asInt());

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

    /**
     * Method allow user to delete cycle meeting agenda.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteCycleMeetingAgenda(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data


        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int result = 0;

        try {
            // If connection is not null then perform delete operation.
            if (con != null) {
                stmt = con.prepareStatement("DELETE FROM " + schemaName
                        + ".cycleMeetingAgenda WHERE id = ?");

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

    }

}

