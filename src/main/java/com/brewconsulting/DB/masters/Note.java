package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.brewconsulting.DB.utils.stringToDate;

/**
 * Created by lcom53 on 17/10/16.
 */
public class Note
{
    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("id")
    public int id;

    @JsonView({ UserViews.groupNoteView.class })
    @JsonProperty("groupId")
    public int groupId;

    @JsonView({ UserViews.childNoteView.class })
    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("title")
    public String title;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("description")
    public String description;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("category")
    public String  category;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("createdOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createOn;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("createdBy")
    public int createBy;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updateOn;

    @JsonView({ UserViews.childNoteView.class, UserViews.groupNoteView.class })
    @JsonProperty("updateBy")
    public int updateBy;

    // make default constructor to visible package
    public Note() {
    }

    public enum NoteCategory
    {
        Prepare_Edit_Content, Venue_Arrangements,Travel_Arrangement,Misc
    }


    /***
     * Method used to get all Notes of specific Group
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Note> getAllGroupNotes(int id , LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        ArrayList<Note> groupNotes = new ArrayList<Note>();
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (con != null) {
                stmt = con
                        .prepareStatement(" SELECT id,groupid,(note).title title,(note).description description , " +
                                " (note).category category, createdon ," +
                                " createdby, updateon, updateby FROM " +
                                schemaName + ".groupNotes where groupid = ? ORDER BY id ASC");
                stmt.setInt(1,id);
                result = stmt.executeQuery();
                while (result.next()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
//                    java.sql.Date createOn= new java.sql.Date(result.getTimestamp(6).getTime());
//                    java.sql.Date date= new java.sql.Date(result.getTimestamp(10).getTime());
//                    System.out.println("Date : " + createOn);
                    java.sql.Date createDate = new java.sql.Date(result.getTimestamp(6).getTime());
                    java.sql.Date updateDate = new java.sql.Date(result.getTimestamp(8).getTime());

                    Note note = new Note();
                    note.id = result.getInt(1);
                    note.groupId = result.getInt(2);
                    note.title = result.getString(3);
                    note.description = result.getString(4);
                    note.category = result.getString(5);
                    note.createOn = createDate;
                    note.createBy = result.getInt(7);
                    note.updateOn = updateDate;
                    note.updateBy = result.getInt(9);

                    groupNotes.add(note);
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

        return groupNotes;
    }

    /***
     * Method used to get particular Group Note
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Note getGroupNoteById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            Note groupNote = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id,groupid,(note).title title,(note).description description ," +
                                    " (note).category category, createdon ," +
                                    " createdby, updateon, updateby FROM " +
                                    schemaName +".groupNotes where id = ? ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        java.sql.Date createDate = new java.sql.Date(result.getTimestamp(6).getTime());
                        java.sql.Date updateDate = new java.sql.Date(result.getTimestamp(8).getTime());

                        groupNote = new Note();
                        groupNote.id = result.getInt(1);
                        groupNote.groupId = result.getInt(2);
                        groupNote.title = result.getString(3);
                        groupNote.description = result.getString(4);
                        groupNote.category = result.getString(5);
                        groupNote.createOn = createDate;
                        groupNote.createBy = result.getInt(7);
                        groupNote.updateOn = updateDate;
                        groupNote.updateBy = result.getInt(9);

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
            return groupNote;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method used to insert Group Note
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addGroupNote(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                NoteCategory categoryType = NoteCategory.valueOf(node.get("category").asText());

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".groupNotes(groupId,note,createdon,createdby,updateon,updateby) values " +
                                        "(?,ROW(?,?,CAST(? AS master.noteCategory)),?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("groupId").asInt());
                stmt.setString(2,node.get("title").asText());
                stmt.setString(3,node.get("description").asText());
                stmt.setString(4,categoryType.name());;
                stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                stmt.setInt(6, loggedInUser.id);
                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                stmt.setInt(8, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Group Task Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupNoteId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    groupNoteId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return groupNoteId;
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
     * Methos used to update Group Note
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateGroupNote(JsonNode node,LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {

                    NoteCategory categoryType =NoteCategory.valueOf(node.get("category").asText());
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    +".groupNotes SET note = ROW(?,?,CAST(? AS master.noteCategory)),"
                                    +"  updateon = ?, updateby = ?"
                                    +" WHERE id = ?");
                    stmt.setString(1,node.get("title").asText());
                    stmt.setString(2,node.get("description").asText());
                    stmt.setString(3,categoryType.name());;
                    stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                    stmt.setInt(5, loggedInUser.id);
                    stmt.setInt(6, node.get("id").asInt());

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
     * Method used to delete Group Note
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteGroupNote(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".groupNotes WHERE id = ?");

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

    //===============================================================================================

    /**
     * Method allows user to get child Note by id
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Note getChildNoteById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            Note childNote = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT cycleMeetingId,(note).title title,(note).description description ," +
                                    " (note).category category, createdon ," +
                                    " createdby, updateon, updateby FROM " +
                                    schemaName + ".cycleMeetingNotes where id = ? ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        childNote = new Note();
                        childNote.id = id;
                        childNote.cycleMeetingId = result.getInt(1);
                        childNote.title = result.getString(2);
                        childNote.description = result.getString(3);
                        childNote.category = result.getString(4);
                        childNote.createOn = new java.sql.Date(result.getTimestamp(5).getTime());
                        childNote.createBy = result.getInt(6);
                        childNote.createOn = new java.sql.Date(result.getTimestamp(7).getTime());
                        childNote.updateBy = result.getInt(8);

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
            return childNote;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method allows user to get All child Note of same cyclemeeting
     *
     * @param cycleMeetingId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Note> getAllChildNote(int cycleMeetingId, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            Note childNote = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ArrayList<Note> notes = new ArrayList<>();

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id,(note).title title,(note).description description ," +
                                    " (note).category category, createdon ," +
                                    " createdby, updateon, updateby FROM " +
                                    schemaName + ".cycleMeetingNotes where cycleMeetingId = ? ");
                    stmt.setInt(1, cycleMeetingId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        childNote = new Note();
                        childNote.id = result.getInt(1);
                        childNote.cycleMeetingId = cycleMeetingId;
                        childNote.title = result.getString(2);
                        childNote.description = result.getString(3);
                        childNote.category = result.getString(4);
                        childNote.createOn = new java.sql.Date(result.getTimestamp(5).getTime());
                        childNote.createBy = result.getInt(6);
                        childNote.createOn = new java.sql.Date(result.getTimestamp(7).getTime());
                        childNote.updateBy = result.getInt(8);

                        notes.add(childNote);

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
            return notes;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /**
     * Method allow user to add child  notes.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addChildNote(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                NoteCategory categoryType = NoteCategory.valueOf(node.get("category").asText());

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cycleMeetingNotes(cycleMeetingId,note,createdon,createdby,updateon,updateby) values " +
                                        "(?,ROW(?,?,CAST(? AS master.noteCategory)),?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setString(2, node.get("title").asText());
                stmt.setString(3, node.get("description").asText());
                stmt.setString(4, categoryType.name());
                ;
                stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                stmt.setInt(6, loggedInUser.id);
                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                stmt.setInt(8, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add child Note Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupNoteId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    groupNoteId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return groupNoteId;
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
     * Method allow user to update child  notes.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateChildNote(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {

                    NoteCategory categoryType = NoteCategory.valueOf(node.get("category").asText());
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".cycleMeetingNotes SET note = ROW(?,?,CAST(? AS master.noteCategory)),"
                                    + "  updateOn = ?, updateBy = ?"
                                    + " WHERE id = ?");
                    stmt.setString(1, node.get("title").asText());
                    stmt.setString(2, node.get("description").asText());
                    stmt.setString(3, categoryType.name());
                    stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                    stmt.setInt(5, loggedInUser.id);
                    stmt.setInt(6, node.get("id").asInt());

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
     * Method allow user to delete child  notes.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteChildNote(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cycleMeetingNotes WHERE id = ?");

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

}

