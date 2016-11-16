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
import java.util.Date;
import java.util.List;

/**
 * Created by lcom53 on 18/10/16.
 */
public class Content {
    @JsonProperty("id")
    public int id;

    @JsonProperty("contentName")
    public String contentName;

    @JsonProperty("contentDesc")
    public String contentDesc;

    @JsonProperty("divId")
    public int divId;

    @JsonProperty("agendaId")
    public int agendaId;

    @JsonProperty("url")
    public String url;

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
    public int contentId;

    @JsonProperty("username")
    public String username;

    @JsonProperty("dayNo")
    public int dayNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("meetingDate")
    public Date meetingDate;
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
     * @param divid
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getAllGroupContents(int meetingId, int divid, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;
            try {
                if (con != null) {

                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby , c1.contentid, c2.contentname, c2.contentdesc, c2.divid, c2.url , " +
                            " c5.username , c3.dayNo " +
                            " FROM " + schemaName + ".groupsessioncontentinfo as c1 " +
                            " inner join " + schemaName + ".content as c2 on c2.id = c1.contentid " +
                            " inner join " + schemaName + ".groupagenda c3 on c3.id = c1.agendaid " +
                            " inner join " + schemaName + ".cyclemeetinggroup c4 on c4.id = c3.groupid " +
                            " inner join master.users as c5 on c5.id = c1.createdby" +
                            " where c4.division = c2.divid and c4.id = ? and c4.division = ?");

                    stmt.setInt(1, meetingId);
                    stmt.setInt(2, divid);
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
                        content.contentId = result.getInt(9);
                        content.contentName = result.getString(10);
                        content.contentDesc = result.getString(11);
                        content.divId = result.getInt(12);
                        content.url = result.getString(13);
                        content.username = result.getString(14);
                        content.dayNo = result.getInt(15);
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
        }
        else
        {
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
                                      String url,  int agendaId, LoggedInUser loggedInUser)
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

            try {
                con.setAutoCommit(false);
                
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

                stmt.setInt(3, divId);
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

                stmt = con.prepareStatement("SELECT max(contentseq) from "+schemaName+".groupsessioncontent where agendaid = ? ");
                stmt.setInt(1,agendaId);
                resultSet = stmt.executeQuery();
                if (resultSet.next())
                {
                    if(resultSet.getInt(1) > 0) {
                        sequenceNo = resultSet.getInt(1);
                        sequenceNo++;
                    }
                    else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }
                }
                else
                {
                    sequenceNo = 0;
                    sequenceNo++;
                }
                System.out.println("Sequence No . : " + sequenceNo);
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
                stmt.setInt(8, contentId);
                result = stmt.executeUpdate();

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
            try {
                con.setAutoCommit(false);
                Integer[] contentArr = new Integer[node.withArray("contentId").size()];
                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("contentId").size(); i++) {
                    contentArr[i] = node.withArray("contentId").get(i).asInt();
                    ContentType typeContent = ContentType.valueOf(node.get("contentType").asText());

                    stmt = con.prepareStatement("SELECT max(contentseq) from "+schemaName+".groupsessioncontent where agendaid = ? ");
                    stmt.setInt(1,node.get("agendaId").asInt());
                    resultSet = stmt.executeQuery();
                    if (resultSet.next())
                    {
                        if(resultSet.getInt(1) > 0) {
                            sequenceNo = resultSet.getInt(1);
                            sequenceNo++;
                        }
                        else {
                            sequenceNo = 0;
                            sequenceNo++;
                        }
                    }
                    else
                    {
                        sequenceNo = 0;
                        sequenceNo++;
                    }
                    System.out.println("Sequence No . : " + sequenceNo);

                    stmt = con.prepareStatement("INSERT INTO " + schemaName + ".groupSessionContentInfo" +
                            " (agendaid,contenttype,contentseq,createdon,createdby , updateon, updatedby,contentid) " +
                            " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                    stmt.setInt(1, node.get("agendaId").asInt());
                    stmt.setString(2, typeContent.name());
                    stmt.setInt(3, 1);
                    stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                    stmt.setInt(5, loggedInUser.id);
                    stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                    stmt.setInt(7, loggedInUser.id);
                    stmt.setInt(8, contentArr[i]);
                    affectedRow = stmt.executeUpdate();
                    affectedRow++;
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
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;
            try {
                if (con != null) {

                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby," +
                            " c1.updateon, c1.updatedby , c1.contentid, c2.contentname, c2.contentdesc, c2.divid, c2.url, c6.username " +
                            " c3.meetingdate " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontentinfo as c1 " +
                            " inner join " + schemaName + ".content  as c2 on c2.id = c1.contentid " +
                            " inner join " + schemaName + ".cyclemeetingagenda as c3 on c3.id = c1.agendaid " +
                            " inner join " + schemaName + ".cyclemeeting as c4 on c4.id = c3.cyclemeetingid " +
                            " inner join " + schemaName + ".cyclemeetinggroup as c5 on c5.id = c4.groupid " +
                            " inner join master.users as c6 on c6.id = c1.createdby" +
                            " where c2.divid = c5.division and c3.cyclemeetingid = ?  and c2.divid = ?");
                    stmt.setInt(1, meetingId);
                    stmt.setInt(2, divid);
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
                        content.contentId = result.getInt(9);
                        content.contentName = result.getString(10);
                        content.contentDesc = result.getString(11);
                        content.divId = result.getInt(12);
                        content.url = result.getString(13);
                        content.username = result.getString(14);
                        content.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(15).getTime())));
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
        }
        else
        {
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
                                             String url, int agendaId, LoggedInUser loggedInUser)
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

            try {
                con.setAutoCommit(false);

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

                stmt.setInt(3, divId);
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

                stmt = con.prepareStatement("SELECT max(contentseq) from "+schemaName+".cyclemeetingsessioncontent where agendaid = ? ");
                stmt.setInt(1,agendaId);
                resultSet = stmt.executeQuery();
                if (resultSet.next())
                {
                    if(resultSet.getInt(1) > 0) {
                        sequenceNo = resultSet.getInt(1);
                        sequenceNo++;
                    }
                    else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }

                }
                else {
                    sequenceNo = 0;
                    sequenceNo++;
                }
                System.out.println("sequenceNo : " + sequenceNo);

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
                stmt.setInt(8, contentId);
                result = stmt.executeUpdate();

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
     * Method used to add Exixting CycleMeeting Content
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
            try {
                con.setAutoCommit(false);
                Integer[] contentArr = new Integer[node.withArray("contentId").size()];
                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("contentId").size(); i++) {
                    contentArr[i] = node.withArray("contentId").get(i).asInt();
                    ContentType typeContent = ContentType.valueOf(node.get("contentType").asText());

                    stmt = con.prepareStatement("SELECT max(contentseq) from "+schemaName+".cyclemeetingsessioncontent where agendaid = ? ");
                    stmt.setInt(1,node.get("agendaId").asInt());
                    resultSet = stmt.executeQuery();
                    if (resultSet.next())
                    {
                        if(resultSet.getInt(1) > 0) {
                            sequenceNo = resultSet.getInt(1);
                            sequenceNo++;
                        }
                        else {
                            sequenceNo = 0;
                            sequenceNo++;
                        }

                    }
                    else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }
                    System.out.println("sequenceNo : " + sequenceNo);

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
                    stmt.setInt(8, contentArr[i]);
                    affectedRow = stmt.executeUpdate();
                    affectedRow++;

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

    /**
     * @param contentName
     * @param contentDesc
     * @param contentType
     * @param divId
     * @param url
     * @param contentSeq
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateGroupContent(String contentName, String contentDesc, String contentType, int divId,
                                         String url, int contentSeq, int agendaId, LoggedInUser loggedInUser, int id)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Content).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement("UPDATE "
                                + schemaName
                                + ".content SET contentName = ?,contentDesc = ?,divId = ?,url=?"
                                + " WHERE id = ?");

                stmt.setString(1, contentName);
                if (contentDesc != null)
                    stmt.setString(2, contentDesc);
                else
                    stmt.setString(2, null);

                stmt.setInt(3, divId);
                stmt.setString(4, url);
                stmt.setInt(5, id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("update content Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int contentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    contentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                ContentType typeContent = ContentType.valueOf(contentType);
                stmt = con
                        .prepareStatement("UPDATE " + schemaName + ".groupsessioncontent" +
                                " SET agendaid=?, contenttype=?, contentseq=?," +
                                " updateon=?, updatedby=?" +
                                " WHERE id=?"
                        );

                stmt.setInt(1, agendaId);
                stmt.setString(2, typeContent.name());
                stmt.setInt(3, contentSeq);
                stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                stmt.setInt(5, loggedInUser.id);
                stmt.setInt(6, id);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("update content Failed.");

                generatedKeys = stmt.getGeneratedKeys();
                int groupContentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    groupContentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                if (typeContent.equals("INFO")) {

                    stmt = con
                            .prepareStatement("UPDATE " + schemaName + ".groupsessioncontent" +
                                    " SET agendaid=?, contenttype=?, contentseq=?," +
                                    " updateon=?, updatedby=?,contentid=?" +
                                    " WHERE id=?"
                            );
                    stmt.setInt(1, agendaId);
                    stmt.setString(2, typeContent.name());
                    stmt.setInt(3, contentSeq);
                    stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                    stmt.setInt(5, loggedInUser.id);
                    stmt.setInt(6, contentId);
                    stmt.setInt(7, id);
                }

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
     * Method used to Group Content
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
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".content WHERE id = ?");

                    stmt.setInt(1, id);
                    result = stmt.executeUpdate();

                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".groupSessionContentInfo WHERE contentid = ?");
                    stmt.setInt(1, id);
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
     *  Method to get All Group Content by specific Agenda
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getGroupContentByAgenda(int agendaId ,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;

            try {
                if(con != null)
                {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby, " +
                            " c1.updateon, c1.updatedby, c1.contentid ,c2.contentname, c2.contentdesc, c2.divid, c2.url , c3.dayNo " +
                            " FROM "+schemaName+".groupsessioncontentinfo as c1 " +
                            " inner join "+schemaName+".content as c2 on c2.id = c1.contentid " +
                            " inner join "+schemaName+".groupagenda as c3 on c3.id = c1.agendaid " +
                            " where  agendaid = ?");
                    stmt.setInt(1,agendaId);
                    result = stmt.executeQuery();
                    while (result.next())
                    {
                        content = new Content();
                        content.id = result.getInt(1);
                        content.agendaId = result.getInt(2);
                        content.contentType = result.getString(3);
                        content.contentSeq = result.getInt(4);
                        content.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        content.createBy = result.getInt(6);
                        content.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        content.updateBy = result.getInt(8);
                        content.contentId = result.getInt(9);
                        content.contentName = result.getString(10);
                        content.contentDesc = result.getString(11);
                        content.divId = result.getInt(12);
                        content.url = result.getString(13);
                        content.dayNo = result.getInt(14);
                        contentList.add(content);
                    }

                }
                else
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
        }
        else
        {
            throw new NotAuthorizedException("");
        }

    }

    /***
     *  Method to get All Cyclemeeting Content by specific Agenda
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Content> getChildContentByAgenda(int agendaId ,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Content> contentList = new ArrayList<>();
            ResultSet result = null;
            Content content;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype, c1.contentseq, c1.createdon, c1.createdby, " +
                            " c1.updateon, c1.updatedby, c1.contentid ,c2.contentname, c2.contentdesc, c2.divid, c2.url , c3.meetingdate " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontentinfo as c1 " +
                            " inner join client1.content as c2 on c2.id = c1.contentid " +
                            " inner join client1.cyclemeetingagenda as c3 on c3.id = c1.agendaid " +
                            " where  agendaid = ?");
                    stmt.setInt(1, agendaId);
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
                        content.contentId = result.getInt(9);
                        content.contentName = result.getString(10);
                        content.contentDesc = result.getString(11);
                        content.divId = result.getInt(12);
                        content.url = result.getString(13);
                        content.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(14).getTime())));
                        contentList.add(content);
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
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}