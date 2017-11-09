package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by lcom53 on 18/10/16.
 */
public class Content {

    @JsonProperty("id")
    public int id;

    @JsonProperty("agendaId")
    public int agendaId;

    @JsonProperty("contentType")
    public String contentType;

    @JsonProperty("contentSeq")
    public int contentSeq;

    @JsonProperty("createBy")
    public int createBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdOn")
    public Date createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("updateOn")
    public Date updateOn;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("contentId")
    public Integer[] contentId;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("dayNo")
    public int dayNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("meetingDate")
    public Date meetingDate;

    @JsonProperty("title")
    public String title;

    @JsonProperty("description")
    public String description;

    @JsonProperty("contentList")
    public List<SettingContent> contentList;

    // make visible to package only
    public Content() {
    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    public static final int Content = 11;

    /***
     * Method to get all GroupContents for specific division and GroupMeetingId
     *
     * @param meetingId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getAllGroupContents(int meetingId, int divId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content = null;
            try {
                if (con != null) {

                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby , c1.contentid, c2.contentname, c2.contentdesc, c2.divid, c2.url , " +
                            " c5.username ,c5.firstname,c5.lastname,(c6.address).city city,(c6.address).state state, " +
                            " (c6.address).phone phone, c3.dayNo,c2.id,c2.createdon,c2.createby " +
                            " FROM " + schemaName + ".groupsessioncontentinfo as c1 " +
                            " inner join " + schemaName + ".content as c2 on c2.id = ANY(c1.contentid ::int[]) " +
                            " inner join " + schemaName + ".groupagenda c3 on c3.id = c1.agendaid " +
                            " inner join " + schemaName + ".cyclemeetinggroup c4 on c4.id = c3.groupid " +
                            " inner join master.users as c5 on c5.id = c2.createby " +
                            " inner join " + schemaName + ".userprofile c6 on c6.userid = c2.createby " +
                            " where (c4.division = c2.divid OR c2.divid IS NULL ) AND c3.groupid = ? AND c4.division = ? " +
                            " ORDER BY c1.contentseq");

                    stmt.setInt(1, meetingId);
                    stmt.setInt(2, divId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        content = new Content();
                        SettingContent settingContent = new SettingContent();
                        content.contentList = new ArrayList<>();

                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.dayNo = result.getInt(20);

                        settingContent.id = result.getInt(21);
                        settingContent.contentName = result.getString(10);
                        settingContent.contentDesc = result.getString(11);
                        settingContent.divId = result.getInt(12);
                        settingContent.url = result.getString(13);
                        settingContent.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(22).getTime())));
                        settingContent.createBy = result.getInt(23);
                        settingContent.userDetails = new ArrayList<>();
                        settingContent.userDetails.add(new UserDetail(result.getInt(23), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));


                        int index = findContent(content.id, contentList);
                        if (index != -1) {
                            contentList.get(index).contentList.add(settingContent);
                        } else {
                            contentList.add(content);
                            if (content.id != 0)
                                content.contentList.add(settingContent);
                        }
                    }

                }

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

            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * method used to add groupContent
     *
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param url
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addGroupContent(String contentName, String contentDesc, String contentType, int divId,
                                      String url, int agendaId, int itemId, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet = null;
            ResultSet contentResult = null;
            int sequenceNo = 0;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement(" SELECT id,contentid from " + schemaName + ".groupSessionContentInfo " +
                        " WHERE agendaid = ? AND id = ? ");
                stmt.setInt(1, agendaId);
                stmt.setInt(2, itemId);
                contentResult = stmt.executeQuery();

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".content(contentName,contentDesc,divid,url,createBy,createdOn) values (?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, contentName);

//                 It checks that description is empty or not
                if (contentDesc != null)
                    stmt.setString(2, contentDesc);
                else
                    stmt.setString(2, null);

                if (divId > 0)
                    stmt.setInt(3, divId);
                else
                    stmt.setNull(3, 0);

                stmt.setString(4, url);
                stmt.setInt(5, loggedInUser.id);
                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add content Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int contentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    contentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                ContentType typeContent = ContentType.valueOf(contentType);

                stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".groupsessioncontent where agendaid = ? ");
                stmt.setInt(1, agendaId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getInt("sequenceNo") > 0) {
                        sequenceNo = resultSet.getInt("sequenceNo");
                        sequenceNo++;
                    } else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }
                } else {
                    sequenceNo = 0;
                    sequenceNo++;
                }

                if (typeContent.name().equals("INFO") || typeContent.name().equals("ACTIVITY")) {
                    if (!contentResult.next()) {
                        // Create contentId Array
                        Integer[] IdArr = new Integer[]{contentId};
                        Array arr = con.createArrayOf("int", IdArr);

                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".groupSessionContentInfo" +
                                " (agendaid,contenttype,contentseq,createdon,createdby , updateon, updatedby,contentid) " +
                                " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                        stmt.setInt(1, agendaId);
                        stmt.setString(2, typeContent.name());
                        stmt.setInt(3, sequenceNo);
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setArray(8, arr);
                        result = stmt.executeUpdate();
                    } else {
                        Integer[] IdArr1;
                        IdArr1 = (Integer[]) contentResult.getArray(2).getArray();
                        System.out.println("Length : " + IdArr1.length);
                        IdArr1 = addElement(IdArr1, contentId);
                        System.out.println("New Array Length : " + IdArr1.length);
                        Array arr1 = con.createArrayOf("int", IdArr1);

                        stmt = con.prepareStatement("UPDATE " + schemaName + ".groupSessionContentInfo " +
                                " SET  contentid = ? WHERE id = ? ");
                        stmt.setArray(1, arr1);
                        stmt.setInt(2, contentResult.getInt(1));

                        result = stmt.executeUpdate();
                    }
                }

                con.commit();
                return contentId;
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
     * Method used to insert existing contents in GroupSessionContent
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addExistingGroupContent(JsonNode node, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            ResultSet resultSet;
            int sequenceNo;
            ResultSet contentResult;
            try {
                con.setAutoCommit(false);
                Integer[] contentArr = new Integer[node.withArray("contentId").size()];
                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("contentId").size(); i++) {

                    stmt = con.prepareStatement("SELECT contentid from " + schemaName + ".groupSessionContentInfo " +
                            " WHERE ? = ANY(contentid::int[]) AND agendaid = ? ");
                    stmt.setInt(1, node.withArray("contentId").get(i).asInt());
                    stmt.setInt(2, node.get("agendaId").asInt());
                    resultSet = stmt.executeQuery();

                    if (!resultSet.next()) {
                        contentArr[i] = node.withArray("contentId").get(i).asInt();
                    }
                }

                ContentType typeContent = ContentType.valueOf(node.get("contentType").asText());

                stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".groupsessioncontent where agendaid = ? ");
                stmt.setInt(1, node.get("agendaId").asInt());
                resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getInt("sequenceNo") > 0) {
                        sequenceNo = resultSet.getInt("sequenceNo");
                        sequenceNo++;
                    } else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }
                } else {
                    sequenceNo = 0;
                    sequenceNo++;
                }

                stmt = con.prepareStatement("SELECT id,contentid from " + schemaName + ".groupSessionContentInfo " +
                        " WHERE agendaid = ? AND id = ?");
                stmt.setInt(1, node.get("agendaId").asInt());
                stmt.setInt(2, node.get("itemId").asInt());
                contentResult = stmt.executeQuery();

                if (typeContent.name().equals("INFO") || typeContent.name().equals("ACTIVITY")) {
                    if (!contentResult.next()) {
                        Array array = con.createArrayOf("int", contentArr);
                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".groupSessionContentInfo" +
                                " (agendaid,contenttype,contentseq,createdon,createdby , updateon, updatedby,contentid) " +
                                " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                        stmt.setInt(1, node.get("agendaId").asInt());
                        stmt.setString(2, typeContent.name());
                        stmt.setInt(3, sequenceNo);
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setArray(8, array);
                        affectedRow = stmt.executeUpdate();
                        affectedRow++;
                    } else {
                        Integer[] IdArr;
                        IdArr = (Integer[]) contentResult.getArray(2).getArray();

                        for (int i = 0; i < contentArr.length; i++) {
                            IdArr = addElement(IdArr, contentArr[i]);
                        }
                        Array array = con.createArrayOf("int", IdArr);

                        stmt = con.prepareStatement("UPDATE " + schemaName + ".groupSessionContentInfo " +
                                " SET  contentid = ? WHERE id = ?");
                        stmt.setArray(1, array);
                        stmt.setInt(2, contentResult.getInt(1));

                        affectedRow = stmt.executeUpdate();
                        affectedRow++;
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
     * Method used to update Sequence Number of Group Content
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateGroupSeqNumber(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                for (int i = 0; i < node.withArray("contentIDs").size(); i++) {
                    stmt = con.prepareStatement("UPDATE " + schemaName + ".groupsessioncontent SET contentseq = ? WHERE id = ? AND agendaid = ?");
                    stmt.setInt(1, i + 1);
                    stmt.setInt(2, node.withArray("contentIDs").get(i).asInt());
                    stmt.setInt(3, node.get("agendaSessionId").asInt());
                    result = stmt.executeUpdate();
                    result++;
                }
                con.commit();
                return result + 1;
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
     * Method used to delete Group Content
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteGroupContent(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".groupSessionContent WHERE id = ?");
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

    // ========================================================================================
    //  Methods of Cyclemeeting Contents

    /***
     * Method used to get all Cycle Meeting Content with spacific division and MeetingId
     *
     * @param meetingId
     * @param divid
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getAllCycleMeetingContents(int meetingId, int divid, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;
            try {
                if (con != null) {

                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby , c1.contentid, c2.contentname, c2.contentdesc, c2.divid, c2.url, c6.username , " +
                            " c6.firstname,c6.lastname,(c7.address).city city,(c7.address).state state,(c7.address).phone phone,c3.meetingdate," +
                            " c2.id,c2.createdon,c2.createby " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontentinfo as c1 " +
                            " inner join " + schemaName + ".content  as c2 on c2.id = ANY(c1.contentid ::int[]) " +
                            " inner join " + schemaName + ".cyclemeetingagenda as c3 on c3.id = c1.agendaid " +
                            " inner join " + schemaName + ".cyclemeeting as c4 on c4.id = c3.cyclemeetingid " +
                            " inner join " + schemaName + ".cyclemeetinggroup as c5 on c5.id = c4.groupid " +
                            " inner join master.users as c6 on c6.id = c2.createby " +
                            " inner join " + schemaName + ".userprofile c7 on c7.userid = c2.createby " +
                            " where (c2.divid = c5.division OR c2.divid IS NULL) and c3.cyclemeetingid = ?  and c5.division = ?" +
                            " ORDER BY c1.contentseq ");

                    stmt.setInt(1, meetingId);
                    stmt.setInt(2, divid);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        content = new Content();
                        SettingContent settingContent = new SettingContent();
                        content.contentList = new ArrayList<>();

                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(20).getTime())));

                        settingContent.id = result.getInt(21);
                        settingContent.contentName = result.getString(10);
                        settingContent.contentDesc = result.getString(11);
                        settingContent.divId = result.getInt(12);
                        settingContent.url = result.getString(13);
                        settingContent.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(22).getTime())));
                        ;
                        settingContent.createBy = result.getInt(23);
                        settingContent.userDetails = new ArrayList<>();
                        settingContent.userDetails.add(new UserDetail(result.getInt(23), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));

                        int index = findContent(content.id, contentList);
                        if (index != -1) {
                            contentList.get(index).contentList.add(settingContent);
                        } else {
                            contentList.add(content);
                            if (content.id != 0)
                                content.contentList.add(settingContent);
                        }
                    }
                }

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

            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /****
     * method used to add Cyclemeeting Content
     *
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param url
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingContent(String contentName, String contentDesc, String contentType, int divId,
                                             String url, int agendaId, int itemId, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet = null;
            int sequenceNo = 0;
            ResultSet contentResult = null;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT id,contentid from " + schemaName + ".cyclemeetingsessioncontentinfo " +
                        " WHERE agendaid = ? AND id = ?");
                stmt.setInt(1, agendaId);
                stmt.setInt(2, itemId);
                contentResult = stmt.executeQuery();

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".content(contentName,contentDesc,divId,url,createBy,createdOn) values (?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, contentName);

//                 It checks that description is empty or not
                if (contentDesc != null)
                    stmt.setString(2, contentDesc);
                else
                    stmt.setString(2, null);

                if (divId > 0)
                    stmt.setInt(3, divId);
                else
                    stmt.setNull(3, 0);
                stmt.setString(4, url);
                stmt.setInt(5, loggedInUser.id);
                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add cycle meeting content Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int contentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    contentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                ContentType typeContent = ContentType.valueOf(contentType);

                stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".cyclemeetingsessioncontent where agendaid = ? ");
                stmt.setInt(1, agendaId);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getInt("sequenceNo") > 0) {
                        sequenceNo = resultSet.getInt("sequenceNo");
                        sequenceNo++;
                    } else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }

                } else {
                    sequenceNo = 0;
                    sequenceNo++;
                }

                if (typeContent.name().equals("INFO") || typeContent.name().equals("ACTIVITY")) {
                    if (!contentResult.next()) {
                        Integer[] IdArr = new Integer[]{contentId};
                        Array arr = con.createArrayOf("int", IdArr);

                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".cyclemeetingsessioncontentinfo" +
                                " (agendaid,contenttype,contentseq,createdon,createdby , updateon, updatedby,contentid) " +
                                " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                        stmt.setInt(1, agendaId);
                        stmt.setString(2, typeContent.name());
                        stmt.setInt(3, sequenceNo);
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setArray(8, arr);
                        result = stmt.executeUpdate();
                    } else {
                        Integer[] IdArr1;
                        IdArr1 = (Integer[]) contentResult.getArray(2).getArray();
                        IdArr1 = addElement(IdArr1, contentId);
                        Array arr1 = con.createArrayOf("int", IdArr1);

                        stmt = con.prepareStatement("UPDATE " + schemaName + ".cyclemeetingsessioncontentinfo " +
                                " SET  contentid = ? WHERE id = ?");
                        stmt.setArray(1, arr1);
                        stmt.setInt(2, contentResult.getInt(1));
                        result = stmt.executeUpdate();
                    }
                }
                con.commit();
                return contentId;
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
     * Method used to add Existing CycleMeeting Content
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addExistingCycleMeetingContent(JsonNode node, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            ResultSet resultSet;
            int sequenceNo;
            ResultSet contentResult;
            try {
                con.setAutoCommit(false);
                Integer[] contentArr = new Integer[node.withArray("contentId").size()];
                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("contentId").size(); i++) {

                    stmt = con.prepareStatement("SELECT contentid from " + schemaName + ".cyclemeetingsessioncontentinfo " +
                            " WHERE ? = ANY(contentid::int[]) AND agendaid = ? ");
                    stmt.setInt(1, node.withArray("contentId").get(i).asInt());
                    stmt.setInt(2, node.get("agendaId").asInt());
                    resultSet = stmt.executeQuery();

                    if (!resultSet.next()) {
                        contentArr[i] = node.withArray("contentId").get(i).asInt();
                    }
                }


                ContentType typeContent = ContentType.valueOf(node.get("contentType").asText());
                stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".cyclemeetingsessioncontent where agendaid = ? ");
                stmt.setInt(1, node.get("agendaId").asInt());
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getInt("sequenceNo") > 0) {
                        sequenceNo = resultSet.getInt("sequenceNo");
                        sequenceNo++;
                    } else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }

                } else {
                    sequenceNo = 0;
                    sequenceNo++;
                }

                stmt = con.prepareStatement("SELECT id,contentid from " + schemaName + ".cyclemeetingsessioncontentinfo " +
                        " WHERE agendaid = ? AND id = ?");
                stmt.setInt(1, node.get("agendaId").asInt());
                stmt.setInt(2, node.get("itemId").asInt());
                contentResult = stmt.executeQuery();

                if (typeContent.name().equals("INFO") || typeContent.name().equals("ACTIVITY")) {
                    if (!contentResult.next()) {
                        Array array = con.createArrayOf("int", contentArr);
                        stmt = con.prepareStatement("INSERT INTO " + schemaName + ".cyclemeetingsessioncontentinfo" +
                                " (agendaid,contenttype,contentseq,createdon,createdby , updateon, updatedby,contentid) " +
                                " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                        stmt.setInt(1, node.get("agendaId").asInt());
                        stmt.setString(2, typeContent.name());
                        stmt.setInt(3, sequenceNo);
                        stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                        stmt.setInt(5, loggedInUser.id);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setArray(8, array);
                        affectedRow = stmt.executeUpdate();
                        affectedRow++;
                    } else {

                        Integer[] IdArr;
                        IdArr = (Integer[]) contentResult.getArray(2).getArray();

                        for (int i = 0; i < contentArr.length; i++) {
                            IdArr = addElement(IdArr, contentArr[i]);
                        }
                        Array array = con.createArrayOf("int", IdArr);

                        stmt = con.prepareStatement("UPDATE " + schemaName + ".cyclemeetingsessioncontentinfo " +
                                " SET  contentid = ? WHERE id = ?");
                        stmt.setArray(1, array);
                        stmt.setInt(2, contentResult.getInt(1));
                        affectedRow = stmt.executeUpdate();
                        affectedRow++;
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
     * Method used to update Sequence Number of Cyclemeeting Content
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateMeetingSeqNumber(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                for (int i = 0; i < node.withArray("contentIDs").size(); i++) {
                    stmt = con.prepareStatement("UPDATE " + schemaName + ".cyclemeetingsessioncontent SET contentseq = ? WHERE id = ? AND agendaid = ?");
                    stmt.setInt(1, i + 1);
                    stmt.setInt(2, node.withArray("contentIDs").get(i).asInt());
                    stmt.setInt(3, node.get("agendaSessionId").asInt());
                    result = stmt.executeUpdate();
                    result++;
                }
                con.commit();
                return result + 1;
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
     * Method used to delete Cyclemeeting Content
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteCycleMeetingContent(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cyclemeetingsessioncontent WHERE id = ?");
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
     * Method to get All Group Content by specific Agenda
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getGroupContentByAgenda(int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content = null;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby, " +
                            " c1.updateon, c1.updatedby, c1.contentid ,c2.contentname, c2.contentdesc, c2.divid, c2.url , c3.dayNo,c2.id " +
                            " FROM (SELECT * FROM " + schemaName + ".groupsessioncontentinfo as c1  WHERE agendaid = ?)c1" +
                            " inner join " + schemaName + ".content as c2 on c2.id = ANY(c1.contentid ::int[]) " +
                            " inner join " + schemaName + ".groupagenda as c3 on c3.id = c1.agendaid " +
                            " ORDER BY c1.contentseq ASC");
                    stmt.setInt(1, agendaId);
                    result = stmt.executeQuery();
                    while (result.next()) {

                        content = new Content();
                        SettingContent settingContent = new SettingContent();
                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.dayNo = result.getInt(14);
                        content.contentList = new ArrayList<>();
                        settingContent.id = result.getInt(15);
                        settingContent.contentName = result.getString(10);
                        settingContent.contentDesc = result.getString(11);
                        settingContent.divId = result.getInt(12);
                        settingContent.url = result.getString(13);


                        int index = findContent(content.id, contentList);
                        if (index != -1) {
                            contentList.get(index).contentList.add(settingContent);
                        } else {
                            contentList.add(content);
                            if (content.id != 0)
                                content.contentList.add(settingContent);
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
            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     * Method to get All Cyclemeeting Content by specific Agenda
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getChildContentByAgenda(int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby, " +
                            " c1.updateon, c1.updatedby, c1.contentid ,c2.contentname, c2.contentdesc, c2.divid, c2.url , c3.meetingdate ,c2.id " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontentinfo as c1 " +
                            " inner join client1.content as c2 on c2.id = ANY(c1.contentid ::int[]) " +
                            " inner join client1.cyclemeetingagenda as c3 on c3.id = c1.agendaid " +
                            " where  agendaid = ? ORDER BY c1.contentseq ASC");
                    stmt.setInt(1, agendaId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        content = new Content();
                        SettingContent settingContent = new SettingContent();
                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(14).getTime())));
                        content.contentList = new ArrayList<>();
                        settingContent.id = result.getInt(15);
                        settingContent.contentName = result.getString(10);
                        settingContent.contentDesc = result.getString(11);
                        settingContent.divId = result.getInt(12);
                        settingContent.url = result.getString(13);

                        int index = findContent(content.id, contentList);
                        if (index != -1) {
                            contentList.get(index).contentList.add(settingContent);
                        } else {
                            contentList.add(content);
                            if (content.id != 0)
                                content.contentList.add(settingContent);
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
            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method is used to get all Mixed group agenda contents From database.
     *
     * @param agendaId
     * @param contentId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getMixedGroupContents(int agendaId, int contentId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;
            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby, c1.contentid,c1.title, c1.description " +
                            " FROM " + schemaName + ".groupsessioncontentinfo c1 " +
                            " where c1.agendaid = ? AND c1.id = ? ");
                    stmt.setInt(1, agendaId);
                    stmt.setInt(2, contentId);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        content = new Content();
                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.title = result.getString(10);
                        content.description = result.getString(11);
                        content.contentList = new ArrayList<>();

                        for (int i = 0; i < content.contentId.length; i++) {
                            stmt = con.prepareStatement("SELECT c2.id,c2.contentname,c2.contentdesc,c2.divid,c2.url " +
                                    " FROM " + schemaName + ".content c2 " +
                                    " WHERE c2.id = ?");
                            stmt.setInt(1, content.contentId[i]);
                            result = stmt.executeQuery();
                            while (result.next()) {
                                SettingContent settingContent = new SettingContent();
                                settingContent.id = result.getInt(1);
                                settingContent.contentName = result.getString(2);
                                settingContent.contentDesc = result.getString(3);
                                settingContent.divId = result.getInt(4);
                                settingContent.url = result.getString(5);
                                content.contentList.add(settingContent);
                            }
                        }
                        contentList.add(content);
                    }
                }

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

            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method is used to get all Mixed Cyclemeeting agenda contents.
     *
     * @param agendaId
     * @param contentId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getMixedMeetingContents(int agendaId, int contentId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Content).equals("Read") ||
                Permissions.isAuthorised(userRole, Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;
            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby, c1.contentid,c1.title, c1.description " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontentinfo c1 " +
                            " where c1.agendaid = ? AND c1.id = ? ");
                    stmt.setInt(1, agendaId);
                    stmt.setInt(2, contentId);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        content = new Content();
                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = (Integer[]) result.getArray(9).getArray();
                        content.title = result.getString(10);
                        content.description = result.getString(11);
                        content.contentList = new ArrayList<>();

                        for (int i = 0; i < content.contentId.length; i++) {
                            stmt = con.prepareStatement("SELECT c2.id,c2.contentname,c2.contentdesc,c2.divid,c2.url " +
                                    " FROM " + schemaName + ".content c2 " +
                                    " WHERE c2.id = ?");
                            stmt.setInt(1, content.contentId[i]);
                            result = stmt.executeQuery();
                            while (result.next()) {
                                SettingContent settingContent = new SettingContent();
                                settingContent.id = result.getInt(1);
                                settingContent.contentName = result.getString(2);
                                settingContent.contentDesc = result.getString(3);
                                settingContent.divId = result.getInt(4);
                                settingContent.url = result.getString(5);
                                content.contentList.add(settingContent);
                            }
                        }
                        contentList.add(content);
                    }
                }

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

            return contentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method is used to remove content id from Array of Group Item Contents
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeGroupContentOfItem(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

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
                                    + ".groupsessioncontentinfo SET contentid = array_remove(contentid, ? )"
                                    + " WHERE id = ?");
                    stmt.setInt(1, node.get("contentId").asInt());
                    stmt.setInt(2, node.get("itemId").asInt());

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
     * Method is used to remove content id from Array of Cycle meeting Item Contents
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeMeetingContentOfItem(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

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
                                    + ".cyclemeetingsessioncontentinfo SET contentid = array_remove(contentid, ?)"
                                    + " WHERE id = ?");
                    stmt.setInt(1, node.get("contentId").asInt());
                    stmt.setInt(2, node.get("itemId").asInt());

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
     * Method is used to add New Element in Existing Array.
     *
     * @param array
     * @param contentId
     * @return
     */
    static Integer[] addElement(Integer[] array, int contentId) {
        array = Arrays.copyOf(array, array.length + 1);
        array[array.length - 1] = contentId;
        return array;
    }

    /***
     * Method used to find content id in array list
     *
     * @param contentid
     * @param list
     * @return
     */
    public static int findContent(int contentid, List<Content> list) {
        for (Content content : list) {
            if (content.id == contentid) {
                return list.indexOf(content);
            }
        }
        return -1;
    }
}