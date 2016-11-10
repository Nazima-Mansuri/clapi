package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.DB.utils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.naming.NamingException;
import javax.rmi.CORBA.Util;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class CycleMeetingAgenda {

    @JsonProperty("id")
    public int id;

    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonProperty("meetingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updatedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updatedOn;

    @JsonProperty("updateBy")
    public int updatedBy;


    // make the default constructor visible to package only.
    CycleMeetingAgenda() {

    }


    /***
     *  Method is used to clone child agenda from parent agenda.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static List<Integer> cloneCycleMeetingAgenda(JsonNode node,LoggedInUser loggedInUser) throws SQLException, NamingException, ClassNotFoundException {
        String schemaName = loggedInUser.schemaName;

        Connection con = DBConnectionProvider.getConn();
        ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        int affectedRows;
        int meetingid = 0;
        List<Integer> idList = new ArrayList<>();
        try {
            con.setAutoCommit(false);

            stmt = con.prepareStatement("SELECT sessionname, sessiondesc, sessionstarttime," +
                                        "sessionendtime FROM client1.groupagenda where groupid = ? and dayNo = ?");
            stmt.setInt(1,node.get("groupid").asInt());
            stmt.setInt(2,node.get("dayNo").asInt());
            result = stmt.executeQuery();
            while (result.next())
            {
                CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                cycleMeetingAgenda.sessionName = result.getString(1);
                cycleMeetingAgenda.sessionDesc = result.getString(2);
                cycleMeetingAgenda.sessionStartTime = result.getTime(3);
                cycleMeetingAgenda.sessionEndTime = result.getTime(4);
                cycleMeetingAgendas.add(cycleMeetingAgenda);
            }

            for (int i=0;i<cycleMeetingAgendas.size();i++)
            {
                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                        + "createdOn,createdBy,updateOn,updatedBy) values (?,?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                stmt.setString(3, cycleMeetingAgendas.get(i).sessionName);
                stmt.setString(4, cycleMeetingAgendas.get(i).sessionDesc);
                stmt.setTime(5, Time.valueOf(cycleMeetingAgendas.get(i).sessionStartTime.toString()));
                stmt.setTime(6, Time.valueOf(cycleMeetingAgendas.get(i).sessionEndTime.toString()));
                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                stmt.setInt(8, loggedInUser.id);
                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                stmt.setInt(10, loggedInUser.id);
                affectedRows = stmt.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Add Cycle Meeting Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                
                if (generatedKeys.next()) {
                    // It gives last inserted Id in divisionId
                    meetingid = generatedKeys.getInt(1);
                    idList.add(meetingid);
                }
                else
                    throw new SQLException("No ID obtained");
            }
            con.commit();
            return idList;
        }
        catch (Exception ex) {
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
                    cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));;
                    cycleMeetingAgenda.createBy = result.getInt(9);
                    cycleMeetingAgenda.updatedOn =new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));;
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
            stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
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
                throw new SQLException("Add Cycle Meeting Failed.");

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
            stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
            stmt.setString(3, node.get("sessionName").asText());
            stmt.setString(4, node.get("sessionDesc").asText());
            stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
            stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
            stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
            stmt.setInt(8, loggedInUser.id);
            stmt.setInt(9, node.get("id").asInt());

            stmt.executeUpdate();
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

    /**
     * method used for get agenda by date
     *
     * @param cycleMeetingId
     * @param date
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingAgenda> getAgendaByDate(int cycleMeetingId, Date date, LoggedInUser loggedInUser)
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
                        .prepareStatement("SELECT id, sessionname, sessiondesc, sessionstarttime," +
                                "sessionendtime,createdon, createdby, updateon, updatedby" +
                                " FROM " + schemaName + ".cyclemeetingagenda where cyclemeetingid= ? and meetingdate = ?");

                stmt.setInt(1,cycleMeetingId);
                stmt.setDate(2, new java.sql.Date(date.getTime()));
                System.out.println(" In METHOD : " + new java.sql.Date(date.getTime()));

                result = stmt.executeQuery();

                while (result.next()) {

                    CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                    cycleMeetingAgenda.id = result.getInt(1);
                    cycleMeetingAgenda.sessionName = result.getString(2);
                    cycleMeetingAgenda.sessionDesc = result.getString(3);
                    cycleMeetingAgenda.sessionStartTime = result.getTime(4);
                    cycleMeetingAgenda.sessionEndTime = result.getTime(5);
                    cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                    cycleMeetingAgenda.createBy = result.getInt(7);
                    cycleMeetingAgenda.updatedOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                    cycleMeetingAgenda.updatedBy = result.getInt(9);
                    cycleMeetingAgenda.meetingDate =new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(date.getTime())));
                    System.out.println("Meeting Date : " + cycleMeetingAgenda.meetingDate);
                    cycleMeetingAgenda.cycleMeetingId=cycleMeetingId;

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

}

