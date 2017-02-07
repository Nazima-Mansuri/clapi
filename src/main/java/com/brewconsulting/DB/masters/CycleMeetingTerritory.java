package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.PushRaven.FcmResponse;
import com.brewconsulting.PushRaven.Notification;
import com.brewconsulting.PushRaven.Pushraven;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by lcom53 on 3/11/16.
 */
public class CycleMeetingTerritory {


    @JsonProperty("id")
    public int id;

    @JsonProperty("cyclemeetingId")
    public int cyclemeetingId;

    @JsonProperty("territoryId")
    public int territoryId;

    @JsonProperty("terrName")
    public String terrName;

    @JsonProperty("username")
    public String username;

    @JsonProperty("fullname")
    public String fullname;

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

    @JsonProperty("profileImage")
    public String profileImage;

    @JsonProperty("userId")
    public int userId;

    public static final int CycleMeetingTerritory=8;
    /***
     * Method used to give all cyclemeeting and territory details.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingTerritory> getAllCycleMeetingTerr(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingTerritory> cycleMeetingTerritories = new ArrayList<CycleMeetingTerritory>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT c.id, cyclemeetingid, territoryid ,t.name ,u.username,u.firstname,u.lastname, " +
                                    " (uf.address).addLine1 addLine1,(uf.address).addLine2 addLine2," +
                                    " (uf.address).addLine3 addLine3,(uf.address).city city,(uf.address).state state," +
                                    " (uf.address).phone phones , uf.profileimage,t1.userid " +
                                    " FROM " + schemaName + ".cyclemeetingterritories c " +
                                    " left join " + schemaName + ".territories t on c.territoryid = t.id" +
                                    " left join " + schemaName + ".userterritorymap t1 on territoryid = t1.terrid" +
                                    " left join master.users u on t1.userid = u.id" +
                                    " left join " + schemaName + ".userprofile uf on t1.userid = uf.userid " +
                                    "where c.cyclemeetingid = ? ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        CycleMeetingTerritory meetingTerritory = new CycleMeetingTerritory();
                        meetingTerritory.id = result.getInt(1);
                        meetingTerritory.cyclemeetingId = result.getInt(2);
                        meetingTerritory.territoryId = result.getInt(3);
                        meetingTerritory.terrName = result.getString(4);
                        meetingTerritory.username = result.getString(5);
                        if(result.getString(6) != null && result.getString(7) != null)
                            meetingTerritory.fullname = result.getString(6) + " " + result.getString(7);
                        else if(result.getString(6) != null && result.getString(7) == null)
                            meetingTerritory.fullname = result.getString(6) + " ";
                        else if(result.getString(6) == null && result.getString(7) != null)
                            meetingTerritory.fullname = result.getString(7);
                        else
                            meetingTerritory.fullname = "";
                        meetingTerritory.addLine1 = result.getString(8);
                        meetingTerritory.addLine2 = result.getString(9);
                        meetingTerritory.addLine3 = result.getString(10);
                        meetingTerritory.city = result.getString(11);
                        meetingTerritory.state = result.getString(12);
                        if (result.getArray(13) != null)
                            meetingTerritory.phones = (String[]) result.getArray(13)
                                    .getArray();
                        meetingTerritory.profileImage = result.getString(14);
                        meetingTerritory.userId = result.getInt(15);
                        cycleMeetingTerritories.add(meetingTerritory);
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
            return cycleMeetingTerritories;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     *
     * @param feedScheduleId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingTerritory> getAllUserDetailsOfFeedSchedule(int feedScheduleId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,CycleMeetingTerritory).equals("Read") ||
                Permissions.isAuthorised(userRole,CycleMeetingTerritory).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<CycleMeetingTerritory> userDetailList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT u.id,u.username,u.firstname,u.lastname,(uf.address).addline1," +
                                    " (uf.address).addline2,(uf.address).addline3,(uf.address).city," +
                                    " (uf.address).state,(uf.address).phone,uf.profileimage" +
                                    " from master.users u " +
                                    " left join "+schemaName+".userprofile uf on uf.userid = u.id" +
                                    " WHERE id = ANY((SELECT userid from "+schemaName+".feedschedule WHERE id = ?) :: int[]) ");
                    stmt.setInt(1, feedScheduleId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        CycleMeetingTerritory userDetail = new CycleMeetingTerritory();
                        userDetail.userId = resultSet.getInt(1);
                        userDetail.username = resultSet.getString(2);
                        userDetail.fullname = resultSet.getString(3) + " " + resultSet.getString(4);
                        userDetail.addLine1 = resultSet.getString(5);
                        userDetail.addLine2 = resultSet.getString(6);
                        userDetail.addLine3 = resultSet.getString(7);
                        userDetail.city = resultSet.getString(8);
                        userDetail.state = resultSet.getString(9);
                        userDetail.phones = (String[]) resultSet.getArray(10).getArray();
                        userDetail.profileImage = resultSet.getString(11);
                        userDetailList.add(userDetail);
                    }

                } else
                    throw new Exception("DB connection is null");
            }
            finally {
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
            return userDetailList;
        }
        else {
            throw new NotAuthorizedException("");
        }
    }
    /***
     *  Insert Cyclemeeting Territory in database
     *  if Cyclemeeting id already exist then delete that records first
     *  after insert new Cyclemeeting Territory record.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingTerr(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            int count = 0;
            ResultSet result;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT id from " + schemaName + ".cyclemeetingterritories WHERE cyclemeetingid = ?");
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                result = stmt.executeQuery();
                if (result.next()) {
                    System.out.println("If called..");
                    stmt = con.prepareStatement(
                            "DELETE FROM " + schemaName + ".cyclemeetingterritories WHERE cyclemeetingid = ?");

                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    affectedRows = stmt.executeUpdate();
                }

                for (int i = 0; i < node.withArray("territoryId").size(); i++) {

                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cyclemeetingterritories(cycleMeetingId,territoryId) values (?,?)");

                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setInt(2, node.withArray("territoryId").get(i).asInt());
                    affectedRows = stmt.executeUpdate();
                    count++;
                }

                if (affectedRows == 0)
                    throw new SQLException("Add CycleMeetingTerritory Failed.");

                con.commit();
                return count;

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
     *  Method used to add Attendance
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingAttendance(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            ResultSet resultSet = null;
            ResultSet updateSet = null;
            int attendanceId = 0;

            try {
                con.setAutoCommit(false);

                Integer[] userIds = new Integer[node.withArray("userId").size()];
                for (int i =0 ; i<node.withArray("userId").size();i++)
                {
                    userIds[i] = node.withArray("userId").get(i).asInt();
                    System.out.println("User ID : " + node.withArray("userId").get(i).asInt());
                }

                Array userArr = con.createArrayOf("int",userIds);

                stmt = con.prepareStatement(" SELECT id,userid FROM "+schemaName+".cyclemeetingattendance " +
                        " WHERE cyclemeetingid = ? ");
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                resultSet = stmt.executeQuery();
                if(resultSet.next())
                {
                    if(node.get("mode").asText().equalsIgnoreCase("App"))
                    {
                        for (int i=0;i<userIds.length;i++)
                        {
                            Integer[] existArr = (Integer[]) resultSet.getArray(2).getArray();
                            Array newArr = con.createArrayOf("int",existArr);

                            stmt = con.prepareStatement(" SELECT count(*) AS Count FROM "+schemaName+".cyclemeetingattendance" +
                                    " WHERE ? = ANY(? :: int[]) ");
                            stmt.setInt(1,userIds[i]);
                            stmt.setArray(2, newArr);
                            updateSet = stmt.executeQuery();
                            if(updateSet.next())
                            {
                                if(updateSet.getInt("Count") == 0)
                                {
                                    stmt = con.prepareStatement(" UPDATE "+schemaName
                                            +".cyclemeetingattendance SET userid = array_append(userid, ? ) WHERE id = ? ");
                                    stmt.setInt(1,userIds[i]);
                                    stmt.setInt(2,resultSet.getInt(1));
                                    attendanceId = stmt.executeUpdate();
                                }
                            }
                        }
                    }

                    if(node.get("mode").asText().equalsIgnoreCase("Web"))
                    {
                        stmt = con.prepareStatement(" UPDATE "+schemaName+".cyclemeetingattendance" +
                                " SET userid = ? WHERE id = ? ");
                        stmt.setArray(1,userArr);
                        stmt.setInt(2,resultSet.getInt(1));
                        stmt.executeUpdate();
                    }
                }
                else
                {
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cyclemeetingattendance(cyclemeetingid,userid,createdate) values (?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);

                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setArray(2, userArr);
                    stmt.setTimestamp(3,new Timestamp((new Date()).getTime()));
                    affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0)
                        throw new SQLException("Add Attendance Failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();

                    if (generatedKeys.next())
                        // It gives last inserted Id in divisionId
                        attendanceId = generatedKeys.getInt(1);
                    else
                        throw new SQLException("No ID obtained");
                }

                con.commit();
                return attendanceId;

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
     *  Method used to add Session Start Time.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<String> addSessionStartTime(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            List<String> sessionList = new ArrayList<>();

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cyclemeetingactualtimes(cyclemeetingid,sessionid,sessionstarttime) values (?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setInt(2, node.get("sessionId").asInt());
                stmt.setTimestamp(3,new Timestamp((new Date()).getTime()));
                affectedRows = stmt.executeUpdate();

                if (affectedRows == 0)
                    throw new SQLException("Add Session Start Time Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int sessionId;
                Date sessionStartTime;
                if (generatedKeys.next()) {
                    // It gives last inserted Id in divisionId
                    sessionId = generatedKeys.getInt(1);
                    sessionStartTime = generatedKeys.getTimestamp(4);
                    sessionList.add(String.valueOf(sessionId));
                    sessionList.add(String.valueOf(sessionStartTime));
                }
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return sessionList;

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
     *  Method used to Update Session End TIme
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateSessionEndTime(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "UPDATE "
                                        + schemaName
                                        + ".cyclemeetingactualtimes SET sessionendtime = ? WHERE id = ? AND sessionid = ? ");

                stmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
                stmt.setInt(2, node.get("id").asInt());
                stmt.setInt(3,node.get("sessionId").asInt());
                affectedRows = stmt.executeUpdate();

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
     *  Method is used to send Push Notification to that users which are present for the exam.
     *
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<String> sendNotification(int meetingId, LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,15).equals("Read") ||
                Permissions.isAuthorised(userRole,15).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            String schemaname = loggedInUser.schemaName;
            String deviceDetails ;
            List<String> allDetails=new ArrayList<>();
            Integer[] idArr = new Integer[2];

            try
            {
                if(con != null)
                {
                    stmt = con.prepareStatement(" SELECT userid FROM "+schemaname+".cyclemeetingattendance" +
                            " WHERE cyclemeetingid = ? GROUP BY userid ");
                    stmt.setInt(1,meetingId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        idArr = (Integer[]) resultSet.getArray(1).getArray();
                        System.out.println("Array Length : " + idArr.length);

                        for(int i=0;i<idArr.length;i++)
                        {
                            System.out.println("ID : "+idArr[i]);
                            deviceDetails = User.getDeviceDetails(idArr[i]);
                            allDetails.add(deviceDetails);
                            System.out.println("Size: " + allDetails.size());
                        }
                    }
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
            return allDetails;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    public static int getAgendaIdFromMeetingId(int meetingId,LoggedInUser loggedInUser) throws Exception
    {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int agendaId = 0;
        String schemaName = loggedInUser.schemaName;

        try
        {
            stmt = con.prepareStatement("SELECT id FROM "+schemaName+".cyclemeetingagenda WHERE cyclemeetingid = ? ");
            stmt.setInt(1,meetingId);
            resultSet = stmt.executeQuery();
            while (resultSet.next())
            {
                agendaId = resultSet.getInt(1);
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
        return agendaId;
    }

    /***
     * Method is used to get All Attendee List which are present for meeting.
     *
     * @param meetingId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Integer> getAllAttendee(int meetingId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,CycleMeetingTerritory).equals("Read") ||
                Permissions.isAuthorised(userRole,CycleMeetingTerritory).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaname = loggedInUser.schemaName;
            List<Integer> attendeeList = new ArrayList<>();
            ResultSet resultSet = null;

            try {
                stmt = con.prepareStatement(" SELECT userid FROM "+schemaname+".cyclemeetingattendance " +
                        " WHERE cyclemeetingid = ? ");
                stmt.setInt(1,meetingId);
                resultSet = stmt.executeQuery();

                while (resultSet.next())
                {
                    Integer[] arr;
                    arr = (Integer[]) resultSet.getArray(1).getArray();
                    for (int i=0;i<arr.length;i++)
                    {
                        attendeeList.add(arr[i]);
                    }
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
            return attendeeList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}
