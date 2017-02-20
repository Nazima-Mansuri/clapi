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
        String roleName = loggedInUser.roles.get(0).roleName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        ResultSet result1 = null;
        String name = null;
        ArrayList<String> statusList = new ArrayList<>();
        try
        {
            stmt = con.prepareStatement("SELECT name from master.roles where id = ? ");
            stmt.setInt(1,2);
            result1 = stmt.executeQuery();
            while (result1.next())
            {
                name = result1.getString(1);
            }
            if(roleName.equals(name))
            {
                CycleMeetingGroup meetingGroup = null;
                CycleMeeting cycleMeeting = null;
                // TODO check authorization
                String schemaName = loggedInUser.schemaName;
                ArrayList<CycleMeetingGroup> groMeetingWrappers = new ArrayList<CycleMeetingGroup>();
                ArrayList<Integer> idList = new ArrayList<>();

                try {
                    if (con != null) {
                        stmt = con
                                .prepareStatement("SELECT c1.id, c1.division, c1.noofdays, c1.leadorganiser,c1.keywords, c1.title, c1.description,c1.createdon, " +
                                        " c1.createdby, c1.updateon, c1.updatedby, c5.username,c5.firstname,c5.lastname,(c6.address).city city, " +
                                        " (c6.address).state state , (c6.address).phone phone, c6.profileimage, " +
                                        " c2.id, c2.title, c2.groupid, c2.venue, c2.startdate, c2.enddate,  " +
                                        " c2.organiser ,c2.createdon, c2.createdby, c2.updatedon, c2.updatedby, c7.username,c7.firstname,c7.lastname, " +
                                        " (c8.address).city city,(c8.address).state state , (c8.address).phone phone, " +
                                        " c3.territoryid, c4.userid " +
                                        " FROM " + schemaName + ".cyclemeetinggroup c1 " +
                                        " LEFT join " + schemaName + ".cyclemeeting c2 on c1.id = c2.groupid  " +
                                        " LEFT join " + schemaName + ".cyclemeetingterritories c3 on c2.id = c3.cyclemeetingid  " +
                                        " LEFT join " + schemaName + ".userterritorymap c4 on c4.terrid = c3.territoryid " +
                                        " LEFT join master.users c5 on c5.id = c1.leadorganiser " +
                                        " LEFT join " + schemaName + ".userprofile c6 on c6.userid = c1.leadorganiser " +
                                        " LEFT join master.users c7 on c7.id = c2.organiser" +
                                        " LEFT join " + schemaName + ".userprofile c8 on c8.userid = c2.organiser " +
                                        " where c1.division = ? AND (c1.leadorganiser = ? OR c2.organiser = ? OR c4.userid = ?) " +
                                        " GROUP BY c1.id,c2.id,c3.territoryid,c4.userid,c5.username,c5.firstname,c5.lastname,c6.address,c6.profileimage, " +
                                        " c7.username,c7.firstname,c7.lastname,c8.address" +
                                        " ORDER BY c1.createdon DESC");
                        stmt.setInt(1, id);
                        stmt.setInt(2,loggedInUser.id);
                        stmt.setInt(3,loggedInUser.id);
                        stmt.setInt(4,loggedInUser.id);
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
                            meetingGroup.userDetails = new ArrayList<>();
                            meetingGroup.userDetails.add(new UserDetail(result.getInt(4), result.getString(12), result.getString(13), result.getString(14), result.getString(15), result.getString(16), (String[]) result.getArray(17).getArray()));
                            meetingGroup.profileImage = result.getString(18);

                            cycleMeeting.id = result.getInt(19);
                            if (cycleMeeting.id != 0) {
                                cycleMeeting.title = result.getString(20);
                                cycleMeeting.groupId = result.getInt(21);
                                cycleMeeting.venue = result.getString(22);
                                cycleMeeting.startDate = result.getTimestamp(23);
                                cycleMeeting.endDate = result.getTimestamp(24);
                                cycleMeeting.organiser = result.getInt(25);
                                cycleMeeting.createDate = result.getTimestamp(26);
                                cycleMeeting.createBy = result.getInt(27);
                                cycleMeeting.updateDate = result.getTimestamp(28);
                                cycleMeeting.updateBy = result.getInt(29);
                                cycleMeeting.userDetails = new ArrayList<>();
                                cycleMeeting.userDetails.add(new UserDetail(result.getInt(25), result.getString(30), result.getString(31), result.getString(32), result.getString(33), result.getString(34), (String[]) result.getArray(35).getArray()));

                                if (cycleMeeting.endDate.before(new Date()) && !cycleMeeting.endDate.equals(new Date())) {
                                    cycleMeeting.status = "Past";
                                    statusList.add("Past");
                                } else if (cycleMeeting.startDate.after(new Date()) && cycleMeeting.endDate.after(new Date()) && !cycleMeeting.endDate.equals(new Date())) {
                                    cycleMeeting.status = "Future";
                                    statusList.add("Future");
                                } else {
                                    cycleMeeting.status = "Current";
                                    statusList.add("Current");
                                }
                            }
                            stmt = con.prepareStatement("SELECT count(cyclemeetingid) from "+schemaName+".cyclemeetingterritories " +
                                    " where cyclemeetingid = ? ");
                            stmt.setInt(1,result.getInt(19));
                            result1 = stmt.executeQuery();
                            while (result1.next())
                            {
                                cycleMeeting.count = result1.getInt(1);
                            }


                            int index = findMeeting(meetingGroup.id, groMeetingWrappers);
                            int meeting = cycleMeeting(cycleMeeting.id,idList);



                            if (index != -1) {
                                if(meeting == -1) {
                                    groMeetingWrappers.get(index).cycleMeetings.add(cycleMeeting);
                                }
                                if(statusList != null && statusList.size() > 0){
                                    if(statusList.stream().allMatch(t -> t.equals("Past"))){
                                        meetingGroup.status = "Past";
                                    }else if(statusList.stream().allMatch(t -> t.equals("Future"))){
                                        meetingGroup.status="Future";
                                    }else if(statusList.stream().allMatch(t -> t.equals("Current"))){
                                        meetingGroup.status="Current";
                                    }else if(statusList.stream().anyMatch(t -> t.equals("Present"))){
                                        meetingGroup.status="Present";
                                    }else if(statusList.stream().anyMatch(t -> t.equals("Future"))){
                                        meetingGroup.status="Future";
                                    }
                                }
                                groMeetingWrappers.get(index).status = meetingGroup.status;


                            } else {
                                if(statusList.size() > 0) {
                                    String last = statusList.get(statusList.size() - 1);
                                    statusList.clear();
                                    meetingGroup.status = last;
                                    statusList.add(last);
                                }
                                groMeetingWrappers.add(meetingGroup);
                                if (cycleMeeting.id != 0)
                                    idList.add(cycleMeeting.id);
                                    meetingGroup.cycleMeetings.add(cycleMeeting);
                            }
                        }
                    } else
                        throw new Exception("DB connection is null");
                } finally {
                    if(result1 != null)
                        if(!result1.isClosed())
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
                return groMeetingWrappers;
            }
            else {
                if (Permissions.isAuthorised(userRole, CycleMeetingGroup).equals("Read") ||
                        Permissions.isAuthorised(userRole, CycleMeetingGroup).equals("Write")) {

                    CycleMeetingGroup meetingGroup = null;
                    CycleMeeting cycleMeeting = null;
                    // TODO check authorization
                    String schemaName = loggedInUser.schemaName;
                    ArrayList<CycleMeetingGroup> groMeetingWrappers = new ArrayList<CycleMeetingGroup>();
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
                                            " (c7.address).state state , (c7.address).phone phone ,count(c6.cyclemeetingid) " +
                                            " FROM " + schemaName + ".cyclemeetinggroup c1 " +
                                            " left join " + schemaName + ".cyclemeeting c2 on c1.id = c2.groupid  " +
                                            " left join master.users c3 on c1.leadorganiser = c3.id  " +
                                            " left join master.users c4 on c2.organiser = c4.id" +
                                            " left join " + schemaName + ".userprofile c5 on c5.userid = c1.leadorganiser" +
                                            " left join " + schemaName + ".userprofile c7 on c7.userid = c2.organiser" +
                                            " left join " + schemaName + ".cyclemeetingterritories c6 on c2.id = c6.cyclemeetingid " +
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
                                meetingGroup.createdOn = result.getTimestamp(8);
                                meetingGroup.createdBy = result.getInt(9);
                                meetingGroup.updateOn = result.getTimestamp(10);
                                meetingGroup.updatedBy = result.getInt(11);
                                meetingGroup.userDetails = new ArrayList<>();
                                meetingGroup.userDetails.add(new UserDetail(result.getInt(4), result.getString(12), result.getString(13), result.getString(14), result.getString(15), result.getString(16), (String[]) result.getArray(17).getArray()));
                                meetingGroup.profileImage = result.getString(18);

                                cycleMeeting.id = result.getInt(19);
                                if (cycleMeeting.id != 0) {
                                    cycleMeeting.title = result.getString(20);
                                    cycleMeeting.groupId = result.getInt(21);
                                    cycleMeeting.venue = result.getString(22);
                                    cycleMeeting.startDate = result.getTimestamp(23);
                                    cycleMeeting.endDate = result.getTimestamp(24);
                                    cycleMeeting.organiser = result.getInt(25);
                                    cycleMeeting.createDate = result.getTimestamp(26);
                                    cycleMeeting.createBy = result.getInt(27);
                                    cycleMeeting.updateDate = result.getTimestamp(28);
                                    cycleMeeting.updateBy = result.getInt(29);
                                    cycleMeeting.userDetails = new ArrayList<>();
                                    cycleMeeting.userDetails.add(new UserDetail(result.getInt(25), result.getString(30), result.getString(31), result.getString(32), result.getString(33), result.getString(34), (String[]) result.getArray(35).getArray()));
                                    cycleMeeting.count = result.getInt(36);
                                    if (cycleMeeting.endDate.before(new Date()) && !cycleMeeting.endDate.equals(new Date())) {
                                        cycleMeeting.status = "Past";
                                        statusList.add("Past");
                                    } else if (cycleMeeting.startDate.after(new Date()) && cycleMeeting.endDate.after(new Date()) && !cycleMeeting.endDate.equals(new Date())) {
                                        cycleMeeting.status = "Future";
                                        statusList.add("Future");
                                    } else {
                                        cycleMeeting.status = "Current";
                                        statusList.add("Current");
                                    }
                                }

                                int index = findMeeting(meetingGroup.id, groMeetingWrappers);

                                if (index != -1) {
                                    if(statusList != null && statusList.size() > 0){
                                        if(statusList.stream().allMatch(t -> t.equals("Past"))){
                                            meetingGroup.status = "Past";
                                        }else if(statusList.stream().allMatch(t -> t.equals("Future"))){
                                            meetingGroup.status="Future";
                                        }else if(statusList.stream().allMatch(t -> t.equals("Current"))){
                                            meetingGroup.status="Current";
                                        }else if(statusList.stream().anyMatch(t -> t.equals("Present"))){
                                            meetingGroup.status="Present";
                                        }else if(statusList.stream().anyMatch(t -> t.equals("Future"))){
                                            meetingGroup.status="Future";
                                        }
                                    }
                                    groMeetingWrappers.get(index).status = meetingGroup.status;
                                    groMeetingWrappers.get(index).cycleMeetings.add(cycleMeeting);

                                } else {
                                    if(statusList.size() > 0) {
                                        String last = statusList.get(statusList.size() - 1);
                                        statusList.clear();
                                        meetingGroup.status = last;
                                        statusList.add(last);
                                    }
                                    groMeetingWrappers.add(meetingGroup);
                                    if (cycleMeeting.id != 0)
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
        }finally {
            if(result1 != null)
                if(result1.isClosed())
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
                        meetingGroup.createdOn = result.getTimestamp(8);
                        meetingGroup.createdBy = result.getInt(9);
                        meetingGroup.updateOn = result.getTimestamp(10);
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
                    // It gives last inserted Id in parentMeetingId
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
     * Method used to find meeting id in Meeting List.
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

    /***
     *  Find cyclemeeting Id in List.
     *
     * @param meetingid
     * @param list
     * @return
     */
    public static int cycleMeeting(int meetingid, List<Integer> list) {
        for (Integer id : list) {
            if (id == meetingid) {
                return list.indexOf(id);
            }
        }
        return -1;
    }
}



