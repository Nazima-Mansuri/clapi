package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupAgenda {

    @JsonProperty("id")
    public int id;

    @JsonProperty("groupId")
    public int groupId;

    @JsonProperty("dayNo")
    public int dayNo;

    @JsonProperty("sessionName")
    public String sessionName;

    @JsonProperty("sessionDesc")
    public String sessionDesc;

    @JsonProperty("sessionStartTime")
    public Time sessionStartTime;

    @JsonProperty("sessionEndTime")
    public Time sessionEndTime;

    @JsonProperty("sessionConductor")
    public String sessionConductor;

    @JsonProperty("createOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updateOn;

    @JsonProperty("updateBy")
    public int updateBy;


    // MAKE THE DEFAULT CONSTRUCTOR VISIBLE TO PACKAGE ONLY.
    GroupAgenda() {

    }

    /**
     * Method Add group agenda in Database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addGroupAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

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
                                    + ".groupAgenda(groupId,dayNo,sessionName,sessionDesc,sessionStartTime,sessionEndTime,sessionConductor,"
                                    + "createdOn,createdBy,updateOn,updatedBy) values (?,?,?,?,?,?,?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, node.get("groupId").asInt());
            stmt.setInt(2, node.get("dayNo").asInt());
            stmt.setString(3, node.get("sessionName").asText());
            stmt.setString(4, node.get("sessionDesc").asText());
            stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
            stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
            stmt.setString(7, node.get("sessionConductor").asText());
            stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
            stmt.setInt(9, loggedInUser.id);
            stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
            stmt.setInt(11, loggedInUser.id);
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
     * Method update group agenda in Database.
     *
     * @param node
     * @param loggedInUser
     * @throws Exception
     */
    public static void updateGroupAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int result;

        try {
            con.setAutoCommit(false);

            stmt = con
                    .prepareStatement(
                            "UPDATE "
                                    + schemaName
                                    + ".groupAgenda SET groupId =? ,dayNo=?,sessionName=?,sessionDesc=?,sessionStartTime=?,sessionEndTime=?,sessionConductor=?,"
                                    + "updateOn=?,updatedBy=? where id=?",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, node.get("groupId").asInt());
            stmt.setInt(2, node.get("dayNo").asInt());
            stmt.setString(3, node.get("sessionName").asText());
            stmt.setString(4, node.get("sessionDesc").asText());
            stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
            stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
            stmt.setString(7, node.get("sessionConductor").asText());
            stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
            stmt.setInt(9, loggedInUser.id);
            stmt.setInt(10, node.get("id").asInt());

            result = stmt.executeUpdate();
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
     * Method for deleting group agenda from database
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteGroupAgenda(int id, LoggedInUser loggedInUser)
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
                        + ".groupAgenda WHERE id = ?");

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


    /**
     * method for get group agenda by dayNo
     *
     * @param groupId
     * @param dayNo
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<GroupAgenda> getAgendaByDay(int groupId, int dayNo, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        String schemaName = loggedInUser.schemaName;

        Connection con = DBConnectionProvider.getConn();
        ArrayList<GroupAgenda> groupAgendas = new ArrayList<GroupAgenda>();
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (con != null) {
                stmt = con
                        .prepareStatement("SELECT id, sessionname, sessiondesc, sessionstarttime," +
                                "sessionendtime, sessionconductor,createdon, createdby, updateon, updatedby" +
                                " FROM " + schemaName + ".groupagenda where groupid= ? and dayno = ?");

                stmt.setInt(1, groupId);
                stmt.setInt(2, dayNo);
                result = stmt.executeQuery();

                while (result.next()) {
                    GroupAgenda groupAgenda = new GroupAgenda();
                    groupAgenda.id = result.getInt(1);
                    groupAgenda.sessionName = result.getString(2);
                    groupAgenda.sessionDesc = result.getString(3);
                    groupAgenda.sessionStartTime = result.getTime(4);
                    groupAgenda.sessionEndTime = result.getTime(5);
                    groupAgenda.sessionConductor = result.getString(6);
                    groupAgenda.createOn = result.getTimestamp(7);
                    groupAgenda.createBy = result.getInt(8);
                    groupAgenda.updateOn = result.getTimestamp(9);
                    groupAgenda.updateBy = result.getInt(10);
                    groupAgenda.groupId=groupId;
                    groupAgenda.dayNo=dayNo;

                    groupAgendas.add(groupAgenda);
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
        return groupAgendas;
    }
}