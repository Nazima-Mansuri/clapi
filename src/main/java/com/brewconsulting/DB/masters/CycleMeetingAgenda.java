package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.naming.NamingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
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

    @JsonProperty("updatedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updatedOn;

    @JsonProperty("updateBy")
    public int updatedBy;

    @JsonProperty("contents")
    public  List<Content> contents;

    public static final int CycleMeetingAgenda = 10;

    // make the default constructor visible to package only.
    CycleMeetingAgenda() {

    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    /***
     * Method is used to clone child agenda from parent agenda.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static List<Integer> cloneCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas  = new ArrayList<>();;
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet resultSet = null;
            int affectedRows;
            int meetingid = 0;
            int groupContentId = 0;
            List<Integer> idList = new ArrayList<>();
            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT g.id,sessionname, sessiondesc, sessionstarttime," +
                        " sessionendtime,g.contenttype,sessionconductor " +
                        " FROM " + schemaName + ".groupagenda g " +
                        " where groupid = ? and dayNo = ?");
                stmt.setInt(1, node.get("groupid").asInt());
                stmt.setInt(2, node.get("dayNo").asInt());
                result = stmt.executeQuery();
                while (result.next()) {
                    CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.sessionName = result.getString(2);
                        cycleMeetingAgenda.sessionDesc = result.getString(3);
                        cycleMeetingAgenda.sessionStartTime = String.valueOf(result.getTime(4));
                        cycleMeetingAgenda.sessionEndTime = String.valueOf(result.getTime(5));
                        cycleMeetingAgenda.contentType = result.getString(6);
                        cycleMeetingAgenda.sessionConductor = result.getString(7);
                        cycleMeetingAgenda.contents = new ArrayList<>();

                    stmt =con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, contentid " +
                            " FROM "+schemaName+".groupsessioncontentinfo WHERE agendaid = ?");
                    stmt.setInt(1,result.getInt(1));
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        Content content = new Content();
                        content.id = resultSet.getInt(1);
                        content.agendaId = resultSet.getInt(2);
                        content.contentType = resultSet.getString(3);
                        content.contentSeq = resultSet.getInt(4);
                        content.contentId = resultSet.getInt(5);
                        cycleMeetingAgenda.contents.add(content);
                    }
                    cycleMeetingAgendas.add(cycleMeetingAgenda);

                }
                System.out.println("Agenda Size : " + cycleMeetingAgendas.size());

                if(cycleMeetingAgendas.size() > 0) {
                    for (int i = 0; i < cycleMeetingAgendas.size(); i++) {
                        ContentType contentType = ContentType.valueOf(cycleMeetingAgendas.get(i).contentType);

                            stmt = con
                                    .prepareStatement(
                                            "INSERT INTO "
                                                    + schemaName
                                                    + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                                    + "createdOn,createdBy,updateOn,updatedBy,contenttype,sessionconductor) values (?,?,?,?,?,?,?,?,?,?,CAST(? AS master.contentType),?)",
                                            Statement.RETURN_GENERATED_KEYS);
                            stmt.setInt(1, node.get("cycleMeetingId").asInt());
                            stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                            stmt.setString(3, cycleMeetingAgendas.get(i).sessionName);
                            stmt.setString(4, cycleMeetingAgendas.get(i).sessionDesc);
                            stmt.setTime(5, Time.valueOf(cycleMeetingAgendas.get(i).sessionStartTime));
                            stmt.setTime(6, Time.valueOf(cycleMeetingAgendas.get(i).sessionEndTime));
                            stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                            stmt.setInt(8, loggedInUser.id);
                            stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                            stmt.setInt(10, loggedInUser.id);
                            stmt.setString(11, contentType.name());
                            stmt.setString(12, cycleMeetingAgendas.get(i).sessionConductor);
                            affectedRows = stmt.executeUpdate();

                            if (affectedRows == 0)
                                throw new SQLException("Add Cycle Meeting Failed.");

                            ResultSet generatedKeys = stmt.getGeneratedKeys();

                            if (generatedKeys.next()) {
                                // It gives last inserted Id in divisionId
                                meetingid = generatedKeys.getInt(1);
                                idList.add(meetingid);
                            } else
                                throw new SQLException("No ID obtained");

                        if(cycleMeetingAgendas.get(i).contents.size() > 0) {
                            for (int j = 0; j < cycleMeetingAgendas.get(i).contents.size(); j++) {
                                stmt = con.prepareStatement("INSERT INTO  "
                                                + schemaName
                                                + ".cyclemeetingsessioncontentinfo (agendaid,contenttype,contentseq,createdon, "
                                                + "createdby,updateon,updatedby,contentid) "
                                                + "VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?)",
                                        Statement.RETURN_GENERATED_KEYS);
                                System.out.println("Meeting Id : " + meetingid);
                                stmt.setInt(1, meetingid);
                                stmt.setString(2, cycleMeetingAgendas.get(i).contents.get(j).contentType);
                                stmt.setInt(3, cycleMeetingAgendas.get(i).contents.get(j).contentSeq);
                                stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                                stmt.setInt(5, loggedInUser.id);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setInt(8, cycleMeetingAgendas.get(i).contents.get(j).contentId);
                                affectedRows = stmt.executeUpdate();
                                if (affectedRows == 0)
                                    throw new SQLException("Add Cycle Meeting Failed.");

                                generatedKeys = stmt.getGeneratedKeys();

                                if (generatedKeys.next()) {
                                    // It gives last inserted Id in divisionId
                                    groupContentId = generatedKeys.getInt(1);
                                } else
                                    throw new SQLException("No ID obtained");
                            }
                        }
                    }
                }
                else
                {
                   idList.add(0);
                    return idList;
                }
                con.commit();
                return idList;
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    if(!con.isClosed())
                     con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                     stmt.close();
            }
        } else {
            throw new NotAuthorizedException("");
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
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, cyclemeetingid, meetingdate, sessionname, sessiondesc, to_char(sessionstarttime::Time, 'HH12:MI AM')," +
                                    "to_char(sessionendtime::Time, 'HH12:MI AM'), createdon, createdby, updateon, updatedby, contenttype , sessionconductor " +
                                    " FROM " + schemaName + ".cyclemeetingagenda ORDER BY sessionstarttime ASC");
                    result = stmt.executeQuery();
                    System.out.print(result);
                    while (result.next()) {

                        CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.cycleMeetingId = result.getInt(2);
                        cycleMeetingAgenda.meetingDate = result.getDate(3);
                        cycleMeetingAgenda.sessionName = result.getString(4);
                        cycleMeetingAgenda.sessionDesc = result.getString(5);
                        cycleMeetingAgenda.sessionStartTime = result.getString(6);
                        cycleMeetingAgenda.sessionEndTime = result.getString(7);
                        cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        cycleMeetingAgenda.createBy = result.getInt(9);
                        cycleMeetingAgenda.updatedOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        cycleMeetingAgenda.updatedBy = result.getInt(11);
                        cycleMeetingAgenda.contentType = result.getString(12);
                        cycleMeetingAgenda.sessionConductor = result.getString(13);

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
        } else {
            throw new NotAuthorizedException("");
        }
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

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet = null;
            int id;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM " + schemaName + ".cycleMeetingAgenda " +
                        " where cyclemeetingid = ? AND  meetingdate = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                stmt.setTime(3, Time.valueOf(node.get("sessionStartTime").asText()));
                stmt.setTime(4, Time.valueOf(node.get("sessionStartTime").asText()));
                stmt.setTime(5, Time.valueOf(node.get("sessionEndTime").asText()));
                stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    ContentType contentType = ContentType.valueOf(node.get("contentType").asText());
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                            + "createdOn,createdBy,updateOn,updatedBy,contenttype,sessionconductor) values (?,?,?,?,?,?,?,?,?,?,CAST(? AS master.contentType),?)",
                                    Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                    stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                    stmt.setInt(8, loggedInUser.id);
                    stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                    stmt.setInt(10, loggedInUser.id);
                    stmt.setString(11, contentType.name());
                    if (node.has("sessionConductor"))
                        stmt.setString(12, node.get("sessionConductor").asText());
                    else
                        stmt.setString(12, null);
                    result = stmt.executeUpdate();

                    if (result == 0)
                        throw new SQLException("Add Cycle Meeting Agenda Failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();

                    if (generatedKeys.next())
                        // It gives last inserted Id in divisionId
                        id = generatedKeys.getInt(1);
                    else
                        throw new SQLException("No ID obtained");

                    con.commit();
                } else {
                    throw new BadRequestException("");
                }
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

        } else {
            throw new NotAuthorizedException("");
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

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet;
            int cyclemeetingId=0;
            Date meetingDate = null;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT cyclemeetingid, meetingdate FROM " + schemaName+".cycleMeetingAgenda WHERE id = ?");
                stmt.setInt(1,node.get("id").asInt());
                resultSet = stmt.executeQuery();

                if(resultSet.next())
                {
                    cyclemeetingId = resultSet.getInt(1);
                    meetingDate = resultSet.getDate(2);
                }
                System.out.println("MEETING ID : " + cyclemeetingId);
                System.out.println("MEETING DATE : " + meetingDate);

                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM " + schemaName + ".cycleMeetingAgenda " +
                        " where id != ? AND cyclemeetingid = ? AND  meetingdate = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1,node.get("id").asInt());
                stmt.setInt(2, cyclemeetingId);
                stmt.setDate(3, (java.sql.Date) meetingDate);
                stmt.setTime(4, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                stmt.setTime(7, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                resultSet = stmt.executeQuery();

                if(!resultSet.next())
                {
                    ContentType contentType = ContentType.valueOf(node.get("contentType").asText());
                    stmt = con
                            .prepareStatement(
                                    "UPDATE "
                                            + schemaName
                                            + ".cycleMeetingAgenda SET cycleMeetingId =? ,meetingDate=?,sessionName=?,sessionDesc=?,sessionStartTime=?,sessionEndTime=?,"
                                            + "updateOn=?,updatedBy=? , contenttype = CAST(? AS master.contentType) , sessionconductor = ? where id=?",
                                    Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
                    stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                    stmt.setInt(8, loggedInUser.id);
                    stmt.setString(9, contentType.name());
                    stmt.setString(10, node.get("sessionConductor").asText());
                    stmt.setInt(11, node.get("id").asInt());

                    stmt.executeUpdate();
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
        } else {
            throw new NotAuthorizedException("");
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
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {

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
        } else {
            throw new NotAuthorizedException("");
        }
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
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
            PreparedStatement stmt = null;
            ResultSet result = null;


            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, sessionname, sessiondesc, to_char(sessionstarttime::Time, 'HH12:MI AM')," +
                                    "to_char(sessionendtime::Time, 'HH12:MI AM'),createdon, createdby, updateon, updatedby,contenttype , sessionconductor " +
                                    " FROM " + schemaName + ".cyclemeetingagenda where cyclemeetingid= ? and meetingdate = ? ORDER BY sessionstarttime ASC");

                    stmt.setInt(1, cycleMeetingId);
                    stmt.setDate(2, new java.sql.Date(date.getTime()));
                    result = stmt.executeQuery();

                    while (result.next()) {

                        CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.sessionName = result.getString(2);
                        cycleMeetingAgenda.sessionDesc = result.getString(3);
                        cycleMeetingAgenda.sessionStartTime = result.getString(4);
                        cycleMeetingAgenda.sessionEndTime = result.getString(5);
                        cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        cycleMeetingAgenda.createBy = result.getInt(7);
                        cycleMeetingAgenda.updatedOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        cycleMeetingAgenda.updatedBy = result.getInt(9);
                        cycleMeetingAgenda.contentType = result.getString(10);
                        cycleMeetingAgenda.sessionConductor = result.getString(11);
                        cycleMeetingAgenda.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(date.getTime())));
                        cycleMeetingAgenda.cycleMeetingId = cycleMeetingId;

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
        } else {
            throw new NotAuthorizedException("");
        }
    }

}

