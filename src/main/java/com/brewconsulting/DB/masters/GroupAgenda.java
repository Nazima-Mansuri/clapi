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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    public String sessionStartTime;

    @JsonProperty("sessionEndTime")
    public String sessionEndTime;

    @JsonProperty("sessionConductor")
    public String sessionConductor;

    @JsonProperty("contentType")
    public String contentType;

    @JsonProperty("createOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateOn;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("contentList")
    public ArrayList<Content> contentList;

    public static final int GroupAgenda = 9;
    // MAKE THE DEFAULT CONSTRUCTOR VISIBLE TO PACKAGE ONLY.
    GroupAgenda() {

    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    /**
     * method for get group agenda by groupid and dayNo
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
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,GroupAgenda).equals("Read") ||
                Permissions.isAuthorised(userRole,GroupAgenda).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<GroupAgenda> groupAgendas = new ArrayList<GroupAgenda>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet contentResult = null;

            try {
                if (con != null) {

                    stmt = con
                            .prepareStatement("SELECT id, sessionname, sessiondesc, to_char(sessionstarttime::Time, 'HH12:MI AM')," +
                                    " to_char(sessionendtime::Time, 'HH12:MI AM'), sessionconductor,createdon, createdby, updateon, updatedby,contenttype " +
                                    " FROM " + schemaName + ".groupagenda where groupid= ? and dayno = ?  " +
                                    " ORDER BY sessionstarttime ASC");

                    stmt.setInt(1, groupId);
                    stmt.setInt(2, dayNo);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        GroupAgenda groupAgenda = new GroupAgenda();
                        groupAgenda.id = result.getInt(1);
                        groupAgenda.sessionName = result.getString(2);
                        groupAgenda.sessionDesc = result.getString(3);
                        groupAgenda.sessionStartTime = result.getString(4);
                        groupAgenda.sessionEndTime = result.getString(5);
                        groupAgenda.sessionConductor = result.getString(6);
                        groupAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        groupAgenda.createBy = result.getInt(8);
                        groupAgenda.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(9).getTime())));
                        groupAgenda.updateBy = result.getInt(10);
                        groupAgenda.contentType = result.getString(11);
                        groupAgenda.groupId=groupId;
                        groupAgenda.dayNo=dayNo;
                        groupAgenda.contentList = new ArrayList<>();

                        if(result.getString(11).equals("MIXED"))
                        {
                            stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby, " +
                                    " c1.updateon, c1.updatedby , c1.contentid , c1.title , c1. description " +
                                    " FROM "+schemaName+".groupsessioncontentinfo as c1 WHERE  c1.agendaid = ?");
                            stmt.setInt(1,result.getInt(1));
                            contentResult = stmt.executeQuery();
                            while (contentResult.next())
                            {
                                Content content = new Content();
                                content.id = contentResult.getInt(1);
                                content.agendaId = contentResult.getInt(2);
                                content.contentType = contentResult.getString(3);
                                content.contentSeq = contentResult.getInt(4);
                                content.createdOn = contentResult.getTimestamp(5);
                                content.createBy = contentResult.getInt(6);
                                content.updateOn = contentResult.getTimestamp(7);
                                content.updateBy = contentResult.getInt(8);
                                content.contentId = (Integer[]) contentResult.getArray(9).getArray();
                                content.title = contentResult.getString(10);
                                content.description = contentResult.getString(11);
                                groupAgenda.contentList.add(content);
                            }
                        }

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
        else
        {
            throw new NotAuthorizedException("");
        }
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
        int userRole = loggedInUser.roles.get(0).roleId;

        if(Permissions.isAuthorised(userRole,GroupAgenda).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet;
            ResultSet seqResultSet;
            int id = 0;
            int sequenceNo;

            try {
                con.setAutoCommit(false);

                ContentType contentType = ContentType.valueOf(node.get("contentType").asText());

                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM "+schemaName+".groupagenda " +
                        " where groupid = ? AND  dayno = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1,node.get("groupId").asInt());
                stmt.setInt(2,node.get("dayNo").asInt());
                stmt.setTime(3,Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(4,Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(5,Time.valueOf(node.get("sessionEndTime").asText().trim()));
                stmt.setTime(6,Time.valueOf(node.get("sessionEndTime").asText().trim()));
                resultSet = stmt.executeQuery();

                if(!resultSet.next())
                {
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".groupAgenda(groupId,dayNo,sessionName,sessionDesc,sessionStartTime,sessionEndTime, sessionConductor ,"
                                            + " createdOn,createdBy,updateOn,updatedBy,contenttype ) values (?,?,?,?,?,?,?,?,?,?,?,CAST(? AS master.contentType))",
                                    Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, node.get("groupId").asInt());
                    stmt.setInt(2, node.get("dayNo").asInt());
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                    if(node.has("sessionConductor"))
                        stmt.setString(7, node.get("sessionConductor").asText());
                    else
                        stmt.setString(7,null);
                    stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                    stmt.setInt(9, loggedInUser.id);
                    stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
                    stmt.setInt(11, loggedInUser.id);
                    stmt.setString(12,contentType.name());
                    result = stmt.executeUpdate();

                    if (result == 0)
                        throw new SQLException("Add Agenda Failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();

                    if (generatedKeys.next())
                        // It gives last inserted Id in id
                        id = generatedKeys.getInt(1);
                    else
                        throw new SQLException("No ID obtained");

                    if(contentType.name().equals("MIXED"))
                    {
                        for(int i=0;i<node.withArray("mixedContentType").size();i++)
                        {
                            stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".groupsessioncontent " +
                                    " WHERE agendaid = ? ");
                            stmt.setInt(1, id);
                            seqResultSet = stmt.executeQuery();

                            if (seqResultSet.next()) {
                                if (seqResultSet.getInt("sequenceNo") > 0) {
                                    sequenceNo = seqResultSet.getInt("sequenceNo");
                                    sequenceNo++;
                                } else {
                                    sequenceNo = 0;
                                    sequenceNo++;
                                }
                            } else {
                                sequenceNo = 0;
                                sequenceNo++;
                            }
                            System.out.println("Seq No : " + sequenceNo);

                            Integer[] array = new Integer[]{};
                            Array arr = con.createArrayOf("int",array);

                            ContentType type = ContentType.valueOf(node.withArray("mixedContentType").get(i).get("contentType").asText());

                            if(type.name().equals("INFO") || type.name().equals("ACTIVITY")) {
                                stmt = con.prepareStatement("INSERT INTO " +
                                        schemaName +
                                        ".groupSessionContentInfo(title,description,agendaid,contenttype,contentseq," +
                                        " createdon,createdby , updateon, updatedby,contentid) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                                stmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());

                                if(node.withArray("mixedContentType").get(i).get("description").asText() != null ||
                                        node.withArray("mixedContentType").get(i).get("description").asText() != "") {
                                    stmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    stmt.setString(2,null);

                                stmt.setInt(3, id);
                                stmt.setString(4, type.name());
                                stmt.setInt(5, sequenceNo);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                stmt.setInt(9, loggedInUser.id);
                                stmt.setArray(10,arr);

                                result = stmt.executeUpdate();
                            }

                            if(type.name().equals("TEST"))
                            {

                            }

                        }
                    }
                    con.commit();
                    return id;
                }
                else
                {
                    throw new BadRequestException("");
                }

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
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method update group agenda in Database.
     *
     * @param node
     * @param loggedInUser
     * @throws Exception
     */
    public static int updateGroupAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if(Permissions.isAuthorised(userRole,GroupAgenda).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            PreparedStatement contentStmt = null;
            int result =0;
            ResultSet resultSet;
            ResultSet seqResultSet;
            int groupId = 0,dayNo = 0;
            int sequenceNo;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT groupid , dayNo FROM "+schemaName+".groupagenda WHERE id = ?");
                stmt.setInt(1,node.get("id").asInt());
                resultSet = stmt.executeQuery();

                if(resultSet.next())
                {
                    groupId = resultSet.getInt(1);
                    dayNo = resultSet.getInt(2);
                }

                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM "+schemaName+".groupagenda " +
                        " where id != ? AND groupid = ? AND  dayno = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1,node.get("id").asInt());
                stmt.setInt(2,groupId);
                stmt.setInt(3,dayNo);
                stmt.setTime(4,Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(5,Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(6,Time.valueOf(node.get("sessionEndTime").asText().trim()));
                stmt.setTime(7,Time.valueOf(node.get("sessionEndTime").asText().trim()));
                resultSet = stmt.executeQuery();

                if(!resultSet.next())
                {

                    ContentType contentType = ContentType.valueOf(node.get("contentType").asText());
                    stmt = con
                            .prepareStatement(
                                    "UPDATE "
                                            + schemaName
                                            + ".groupAgenda SET groupId =? ,dayNo=?,sessionName=?,sessionDesc=?,sessionStartTime=?,sessionEndTime=?,sessionConductor=?,"
                                            + "updateOn=?,updatedBy=? , contenttype = CAST(? AS master.contentType) where id=?");
                    stmt.setInt(1, node.get("groupId").asInt());
                    stmt.setInt(2, node.get("dayNo").asInt());
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                    stmt.setString(7, node.get("sessionConductor").asText());
                    stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                    stmt.setInt(9, loggedInUser.id);
                    stmt.setString(10,contentType.name());
                    stmt.setInt(11, node.get("id").asInt());

                    if(contentType.name().equals("MIXED")){

                        contentStmt = con.prepareStatement("DELETE FROM "+schemaName+" " +
                                " .groupsessioncontentinfo WHERE agendaid = ? ");
                        contentStmt.setInt(1,node.get("id").asInt());
                        contentStmt.executeUpdate();

                        for(int i=0;i<node.withArray("mixedContentType").size();i++)
                        {
                            contentStmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".groupsessioncontent " +
                                    " WHERE agendaid = ? ");
                            contentStmt.setInt(1,node.get("id").asInt());
                            seqResultSet = contentStmt.executeQuery();

                            if (seqResultSet.next()) {
                                if (seqResultSet.getInt("sequenceNo") > 0) {
                                    sequenceNo = seqResultSet.getInt("sequenceNo");
                                    sequenceNo++;
                                } else {
                                    sequenceNo = 0;
                                    sequenceNo++;
                                }
                            } else {
                                sequenceNo = 0;
                                sequenceNo++;
                            }

                            Integer[] array = new Integer[]{};
                            Array arr = con.createArrayOf("int",array);

                            ContentType type = ContentType.valueOf(node.withArray("mixedContentType").get(i).get("contentType").asText());

                            if(type.name().equals("INFO") || type.name().equals("ACTIVITY")) {

                                contentStmt = con.prepareStatement("INSERT INTO " +
                                        schemaName +
                                        ".groupSessionContentInfo(title,description,agendaid,contenttype,contentseq," +
                                        " createdon,createdby , updateon, updatedby,contentid) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                                contentStmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());

                                if(node.withArray("mixedContentType").get(i).get("description").asText() != null ||
                                        node.withArray("mixedContentType").get(i).get("description").asText() != "") {
                                    contentStmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    contentStmt.setString(2,null);

                                contentStmt.setInt(3, node.get("id").asInt());
                                contentStmt.setString(4, type.name());
                                contentStmt.setInt(5, sequenceNo);
                                contentStmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(7, loggedInUser.id);
                                contentStmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(9, loggedInUser.id);
                                contentStmt.setArray(10,arr);

                                contentStmt.executeUpdate();
                            }

                            if(type.name().equals("TEST"))
                            {
                                // If content type is test it add contents in test table
                            }
                        }
                    }
                    result = stmt.executeUpdate();
                    con.commit();
                }
                else
                {
                    throw new BadRequestException("");
                }

            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
            return result;

        }
        else
        {
            throw new NotAuthorizedException("");
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

        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,GroupAgenda).equals("Write"))
        {
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
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}