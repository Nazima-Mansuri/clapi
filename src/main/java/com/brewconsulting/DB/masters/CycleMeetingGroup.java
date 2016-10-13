package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createdOn;

    @JsonProperty("createdBy")
    public int createdBy;

    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updateOn;

    @JsonProperty("updatedBy")
    public int updatedBy;

    @JsonProperty("children")
    public ArrayList<CycleMeeting> cycleMeetings;


    // make the default constructor visible to package only.
    public CycleMeetingGroup() {
    }


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

        if (Permissions.isAuthorised(userRole, Permissions.TERRITORY, Permissions.getAccessLevel(userRole))) {

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
                                    " c1.createdon, c1.createdby, c1.updateon, c1.updatedby, " +
                                    " c2.id, c2.title, c2.groupid, c2.venue, c2.startdate, c2.enddate, " +
                                    "c2.organiser, c2.createdon, " +
                                    " c2.createdby, c2.updatedon, c2.updatedby " +
                                    " FROM client1.cyclemeetinggroup c1 " +
                                    "left join client1.cyclemeeting c2 on c1.id = c2.groupid  " +
                                    "where c1.division = ? ORDER BY c1.id ASC");
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
                        meetingGroup.createdOn = result.getTimestamp(8);
                        meetingGroup.createdBy = result.getInt(9);
                        meetingGroup.updateOn = result.getTimestamp(10);
                        meetingGroup.updatedBy = result.getInt(11);

                        cycleMeeting.id = result.getInt(12);
                        cycleMeeting.title = result.getString(13);
                        cycleMeeting.groupId = result.getInt(14);
                        cycleMeeting.venue = result.getString(15);
                        cycleMeeting.startDate = result.getTimestamp(16);
                        cycleMeeting.endDate = result.getTimestamp(17);
                        cycleMeeting.organiser = result.getInt(18);
                        cycleMeeting.createDate = result.getTimestamp(19);
                        cycleMeeting.createBy = result.getInt(20);
                        cycleMeeting.updateDate = result.getTimestamp(21);
                        cycleMeeting.updateBy = result.getInt(22);

                        int index = findMeeting(meetingGroup.id, groMeetingWrappers);
                        if (index > 0) {
                            groMeetingWrappers.get(index).cycleMeetings.add(cycleMeeting);
                        } else {
                            groMeetingWrappers.add(meetingGroup);
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

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

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
     * @param id
     * @param list
     * @return
     */
    public static int findMeeting(int id, List<CycleMeetingGroup> list) {
        for (CycleMeetingGroup cycleMeetingGroup : list) {
            if (cycleMeetingGroup.id == id) {
                return list.indexOf(cycleMeetingGroup);
            }
        }
        return 0;
    }
}



