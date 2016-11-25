package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by lcom53 on 7/10/16.
 */


public class CycleMeetingGroup {

    @JsonProperty("id")
    public int id;

    @JsonProperty("division")
    public int division;

    @JsonProperty("noOfDays")
    public int noOfDays;

    @JsonProperty("leadOrganiser")
    public int leadOrganiser;

    @JsonProperty("keywords")
    public String[] keywords;

    @JsonProperty("title")
    public String title;

    @JsonProperty("description")
    public String description;

    @JsonProperty("createdOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createdOn;

    @JsonProperty("createdBy")
    public int createdBy;

    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateOn;

    @JsonProperty("updatedBy")
    public int updatedBy;

    @JsonProperty("divName")
    public String divName;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("profileImage")
    public String profileImage;

    @JsonProperty("status")
    public String status;

    @JsonProperty("children")
    public ArrayList<CycleMeeting> cycleMeetings;


    // make the default constructor visible to package only.
    public CycleMeetingGroup() {
    }
    public static final int CycleMeetingGroup = 7;

    /**
     * Method used to get all group meeting with sub meetings.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingGroup> getMeetingByDivisionId(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole,CycleMeetingGroup).equals("Read") ||
                Permissions.isAuthorised(userRole,CycleMeetingGroup).equals("Write") ) {

            CycleMeetingGroup meetingGroup = null;
            CycleMeeting cycleMeeting = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            ArrayList<CycleMeetingGroup> groMeetingWrappers = new ArrayList<CycleMeetingGroup>();
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT c1.id, c1.division, c1.noofdays, c1.leadorganiser, " +
                                    " c1.keywords, c1.title, c1.description, " +
                                    " c1.createdon, c1.createdby, c1.updateon, c1.updatedby, c3.username,c3.firstname,c3.lastname,  " +
                                    " (c5.address).city city,(c5.address).state state , (c5.address).phone phone, c5.profileimage , " +
                                    " c2.id, c2.title, c2.groupid, c2.venue, c2.startdate, c2.enddate, " +
                                    " c2.organiser, c2.createdon, " +
                                    " c2.createdby, c2.updatedon, c2.updatedby, c4.username,c4.firstname,c4.lastname,(c7.address).city city," +
                                    " (c7.address).state state , (c7.address).phone phone ,count(c6.cyclemeetingid) "+
                                    " FROM "+schemaName+".cyclemeetinggroup c1 " +
                                    " left join "+schemaName+".cyclemeeting c2 on c1.id = c2.groupid  " +
                                    " left join master.users c3 on c1.leadorganiser = c3.id  " +
                                    " left join master.users c4 on c2.organiser = c4.id" +
                                    " left join "+schemaName+".userprofile c5 on c5.userid = c1.leadorganiser" +
                                    " left join "+schemaName+".userprofile c7 on c7.userid = c2.organiser" +
                                    " left join "+ schemaName +".cyclemeetingterritories c6 on c2.id = c6.cyclemeetingid " +
                                    " where c1.division = ? GROUP BY c1.id,c3.username,c5.address,c5.profileimage,c2.id,c4.username,c7.address," +
                                    " c3.firstname,c3.lastname,c4.firstname,c4.lastname " +
                                    " ORDER BY c1.createdon DESC ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        meetingGroup = new CycleMeetingGroup();
                        cycleMeeting = new CycleMeeting();
                        meetingGroup.cycleMeetings = new ArrayList<>();
                        meetingGroup.id = result.getInt(1);
                        meetingGroup.division = result.getInt(2);
                        meetingGroup.noOfDays = result.getInt(3);
                        meetingGroup.leadOrganiser = result.getInt(4);
                        meetingGroup.keywords = (String[]) result.getArray(5).getArray();
                        meetingGroup.title = result.getString(6);
                        meetingGroup.description = result.getString(7);
                        meetingGroup.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        meetingGroup.createdBy = result.getInt(9);
                        meetingGroup.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        meetingGroup.updatedBy = result.getInt(11);
                        meetingGroup.userDetails = new ArrayList<>();
                        meetingGroup.userDetails.add(new UserDetail(result.getInt(4),result.getString(12),result.getString(13),result.getString(14),result.getString(15),result.getString(16),(String[]) result.getArray(17).getArray()));
                        meetingGroup.profileImage = result.getString(18);

                        cycleMeeting.id = result.getInt(19);
                        if (cycleMeeting.id != 0) {
                            cycleMeeting.title = result.getString(20);
                            cycleMeeting.groupId = result.getInt(21);
                            cycleMeeting.venue = result.getString(22);
                            cycleMeeting.startDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(23).getTime())));
                            cycleMeeting.endDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(24).getTime())));
                            cycleMeeting.organiser = result.getInt(25);
                            cycleMeeting.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(26).getTime())));
                            cycleMeeting.createBy = result.getInt(27);
                            cycleMeeting.updateDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(28).getTime())));
                            cycleMeeting.updateBy = result.getInt(29);
                            cycleMeeting.userDetails = new ArrayList<>();
                            cycleMeeting.userDetails.add(new UserDetail(result.getInt(25),result.getString(30),result.getString(31),result.getString(32),result.getString(33),result.getString(34), (String[]) result.getArray(35).getArray()));
                            cycleMeeting.count = result.getInt(36);
                            if(cycleMeeting.endDate.before(new Date()) && !cycleMeeting.endDate.equals(new Date()))
                            {
                                cycleMeeting.status = "Past";
                            }
                            else if(cycleMeeting.startDate.after(new Date()) && cycleMeeting.endDate.after(new Date()) && !cycleMeeting.endDate.equals(new Date())) {
                                cycleMeeting.status = "Future";
                            }
                            else {
                                cycleMeeting.status = "Current";
                            }
                        }

                        int index = findMeeting(meetingGroup.id, groMeetingWrappers);
                        if (index !=-1) {
                            groMeetingWrappers.get(index).cycleMeetings.add(cycleMeeting);

                        } else {
                            groMeetingWrappers.add(meetingGroup);
                            if(cycleMeeting.id!=0)
                                meetingGroup.cycleMeetings.add(cycleMeeting);
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
            return groMeetingWrappers;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method used to get particular group meeting details.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static CycleMeetingGroup getGroupById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole,CycleMeetingGroup).equals("Read") ||
                Permissions.isAuthorised(userRole,CycleMeetingGroup).equals("Write")) {

            CycleMeetingGroup meetingGroup = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT c1.id, division, noofdays, leadorganiser, keywords,  " +
                                    " title, c1.description, createdon, createdby, updateon, updatedby , u.username ,u.firstname , u.lastname, " +
                                    " d.name as divName , (uf.address).city city, (uf.address).state state, (uf.address).phone phone " +
                                    "  FROM "+schemaName+".cyclemeetinggroup c1 left join master.users u " +
                                    "  on c1.leadorganiser = u.id " +
                                    " left join "+schemaName+".divisions d on d.id = division " +
                                    " left join "+schemaName+".userprofile uf on uf.userid = leadorganiser " +
                                    " where c1.id = ?");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        meetingGroup = new CycleMeetingGroup();
                        meetingGroup.id = result.getInt(1);
                        meetingGroup.division = result.getInt(2);
                        meetingGroup.noOfDays = result.getInt(3);
                        meetingGroup.leadOrganiser = result.getInt(4);
                        meetingGroup.keywords = (String[]) result.getArray(5).getArray();
                        meetingGroup.title = result.getString(6);
                        meetingGroup.description = result.getString(7);
                        meetingGroup.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        meetingGroup.createdBy = result.getInt(9);
                        meetingGroup.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        meetingGroup.updatedBy = result.getInt(11);
                        meetingGroup.userDetails = new ArrayList<>();
                        meetingGroup.userDetails.add(new UserDetail(result.getInt(4),result.getString(12),result.getString(13),result.getString(14),result.getString(16),result.getString(17), (String[]) result.getArray(18).getArray()));
                        meetingGroup.divName = result.getString(15);
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
            return meetingGroup;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method used to insert new Meeting
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeeting(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingGroup).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt;
            int result;


            try {
                con.setAutoCommit(false);

                String[] keywordArr = new String[node.withArray("keywords").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("keywords").size(); i++) {
                    keywordArr[i] = node.withArray("keywords").get(i).asText();
                }

                Array keyarr = con.createArrayOf("text", keywordArr);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cycleMeetingGroup(division,noOfDays,leadOrganiser,keywords,title,description,"
                                        + "createdOn,createdBy,updateOn,updatedBy) values (?,?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("division").asInt());

                stmt.setInt(2, node.get("noOfDays").asInt());

                stmt.setInt(3, node.get("leadOrganiser").asInt());

                stmt.setArray(4, keyarr);

                stmt.setString(5, node.get("title").asText());

                stmt.setString(6, node.get("description").asText());

                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));

                stmt.setInt(8, loggedInUser.id);

                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));

                stmt.setInt(10, loggedInUser.id);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Parent Meeting Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int parentMeetingId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    parentMeetingId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return parentMeetingId;

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
     * Method used to update Meeting
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateCycleMeeting(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingGroup).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt;
            int result;


            try {
                con.setAutoCommit(false);

                String[] keywordArr = new String[node.withArray("keywords").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("keywords").size(); i++) {
                    keywordArr[i] = node.withArray("keywords").get(i).asText();
                }

                Array keyarr = con.createArrayOf("text", keywordArr);

                stmt = con.prepareStatement("UPDATE " + schemaName + ".cycleMeetingGroup SET division = ? , noOfDays = ?, " +
                        "leadOrganiser = ? , keywords = ?, title = ?, description = ?, updateOn = ?, updatedBy = ? " +
                        "WHERE id = ?");

                stmt.setInt(1, node.get("division").asInt());

                stmt.setInt(2, node.get("noOfDays").asInt());

                stmt.setInt(3, node.get("leadOrganiser").asInt());

                stmt.setArray(4, keyarr);

                stmt.setString(5, node.get("title").asText());

                stmt.setString(6, node.get("description").asText());

                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));

                stmt.setInt(8, loggedInUser.id);

                stmt.setInt(9, node.get("id").asInt());

                result = stmt.executeUpdate();

                con.commit();
                return result;

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
     * Method used to delete Meeting
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteCycleMeeting(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole,CycleMeetingGroup).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cycleMeetingGroup WHERE id = ?");

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

    /**
     * Method used to find meeting by id.
     *
     * @param meetingid
     * @param list
     * @return
     */
    public static int findMeeting(int meetingid, List<CycleMeetingGroup> list) {
        for (CycleMeetingGroup cycleMeetingGroup : list) {
            if (cycleMeetingGroup.id == meetingid) {
                return list.indexOf(cycleMeetingGroup);
            }
        }
        return -1;
    }
}



