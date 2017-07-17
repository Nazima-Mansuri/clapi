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
public class Note {
    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("id")
    public int id;

    @JsonView({UserViews.groupNoteView.class})
    @JsonProperty("groupId")
    public int groupId;

    @JsonView({UserViews.childNoteView.class})
    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("title")
    public String title;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("description")
    public String description;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("category")
    public String category;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("createdOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createOn;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("createdBy")
    public int createBy;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateOn;

    @JsonView({UserViews.childNoteView.class, UserViews.groupNoteView.class})
    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    public static final int Note = 12;

    // make default constructor to visible package
    public Note() {
    }

    public enum NoteCategory {
        Prepare_Edit_Content, Venue_Arrangements, Travel_Arrangement, Misc
    }


    /***
     * Method used to get all Notes of specific Group
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Note> getAllGroupNotes(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Note).equals("Read") ||
                Permissions.isAuthorised(userRole, Note).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Note> groupNotes = new ArrayList<Note>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(note).title title,(note).description description , " +
                                    " (note).category category, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby , u.username ,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone " +
                                    " FROM (SELECT * FROM " + schemaName + ".groupNotes g " +
                                    " WHERE groupid = ? AND g.createdby = ?)g " +
                                    " left join master.users u on u.id = g.updateby " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = g.updateby" +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, id);
                    stmt.setInt(2, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Note note = new Note();
                        note.id = result.getInt(1);
                        note.groupId = result.getInt(2);
                        note.title = result.getString(3);
                        note.description = result.getString(4);
                        note.category = result.getString(5);
                        note.createOn = result.getTimestamp(6);
                        note.createBy = result.getInt(7);
                        note.updateOn = result.getTimestamp(8);
                        note.updateBy = result.getInt(9);
                        note.userDetails = new ArrayList<>();
                        note.userDetails.add(new UserDetail(result.getInt(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), result.getString(14), (String[]) result.getArray(15).getArray()));

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
        } else {
            throw new NotAuthorizedException("");
        }
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

        if (Permissions.isAuthorised(userRole, Note).equals("Read") ||
                Permissions.isAuthorised(userRole, Note).equals("Write")) {

            Note groupNote = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT g.id,groupid,(note).title title,(note).description description ," +
                                    " (note).category category, createdon ,createdby, updateon, updateby, u.username,u.firstname,u.lastname, " +
                                    " (uf.address).city city,(uf.address).state state,(uf.address).phone phone  " +
                                    " FROM (SELECT * FROM " + schemaName + ".groupNotes g where g.id = ?)g " +
                                    " left join master.users u on u.id = createdby " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = updateby");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        groupNote = new Note();
                        groupNote.id = result.getInt(1);
                        groupNote.groupId = result.getInt(2);
                        groupNote.title = result.getString(3);
                        groupNote.description = result.getString(4);
                        groupNote.category = result.getString(5);
                        groupNote.createOn = result.getTimestamp(6);
                        groupNote.createBy = result.getInt(7);
                        groupNote.updateOn = result.getTimestamp(8);
                        groupNote.updateBy = result.getInt(9);
                        groupNote.userDetails = new ArrayList<>();
                        groupNote.userDetails.add(new UserDetail(result.getInt(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), result.getString(14), (String[]) result.getArray(15).getArray()));

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

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
                    throw new SQLException("Add Group Note Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupNoteId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupNoteId
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
    public static int updateGroupNote(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
                                    + ".groupNotes SET note = ROW(?,?,CAST(? AS master.noteCategory)),"
                                    + "  updateon = ?, updateby = ?"
                                    + " WHERE id = ?");
                    stmt.setString(1, node.get("title").asText());
                    stmt.setString(2, node.get("description").asText());
                    stmt.setString(3, categoryType.name());
                    ;
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

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
    // Method for Cycle Meeting Notes.

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

        if (Permissions.isAuthorised(userRole, Note).equals("Read") ||
                Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
                                    " (note).category category, c.createdon ," +
                                    " c.createdby, c.updateon, c.updateby , u.username , u.firstname, u.lastname, " +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone FROM (SELECT * FROM " + schemaName + ".cycleMeetingNotes c" +
                                    " WHERE c.id = ?)c" +
                                    " left join master.users u on u.id = createdby " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = c.updateby ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        childNote = new Note();
                        childNote.id = id;
                        childNote.cycleMeetingId = result.getInt(1);
                        childNote.title = result.getString(2);
                        childNote.description = result.getString(3);
                        childNote.category = result.getString(4);
                        childNote.createOn = result.getTimestamp(5);
                        childNote.createBy = result.getInt(6);
                        childNote.updateOn = result.getTimestamp(7);
                        childNote.updateBy = result.getInt(8);
                        childNote.userDetails = new ArrayList<>();
                        childNote.userDetails.add(new UserDetail(result.getInt(8), result.getString(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), (String[]) result.getArray(14).getArray()));

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

        if (Permissions.isAuthorised(userRole, Note).equals("Read") ||
                Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
                            .prepareStatement("SELECT c1.id,(note).title title,(note).description description ," +
                                    " (note).category category, c1.createdon ," +
                                    " c1.createdby, c1.updateon, c1.updateby, u.username,u.firstname,u.lastname, " +
                                    " (uf.address).city city, (uf.address).state state,(uf.address).phone phone" +
                                    " FROM (SELECT * FROM " + schemaName + ".cycleMeetingNotes c1 " +
                                    " WHERE cycleMeetingId = ? AND c1.createdby = ?)c1 " +
                                    " left join master.users u on u.id = createdby " +
                                    " left join " + schemaName + ".userprofile uf ON uf.userid = c1.updateby " +
                                    " ORDER BY c1.createdon DESC");
                    stmt.setInt(1, cycleMeetingId);
                    stmt.setInt(2, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        childNote = new Note();
                        childNote.id = result.getInt(1);
                        childNote.cycleMeetingId = cycleMeetingId;
                        childNote.title = result.getString(2);
                        childNote.description = result.getString(3);
                        childNote.category = result.getString(4);
                        childNote.createOn = result.getTimestamp(5);
                        childNote.createBy = result.getInt(6);
                        childNote.updateOn = result.getTimestamp(7);
                        childNote.updateBy = result.getInt(8);
                        childNote.userDetails = new ArrayList<>();
                        childNote.userDetails.add(new UserDetail(result.getInt(8), result.getString(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), (String[]) result.getArray(14).getArray()));

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

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {

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
                    throw new SQLException("Add Cycle Meeting Note Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupNoteId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupNoteId
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

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {

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

        if (Permissions.isAuthorised(userRole, Note).equals("Write")) {
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

    /***
     * It gives all notes of Logged in User
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Note> getAllNotesOfLogInUser(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Note).equals("Read") ||
                Permissions.isAuthorised(userRole, Note).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Note> groupNotes = new ArrayList<Note>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(note).title title,(note).description description , " +
                                    " (note).category category, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby , u.username ,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone " +
                                    " FROM (SELECT * FROM " + schemaName + ".groupNotes g WHERE g.createdby = ?)g " +
                                    " left join master.users u on u.id = g.updateby " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = g.updateby " +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Note note = new Note();
                        note.id = result.getInt(1);
                        note.groupId = result.getInt(2);
                        note.title = result.getString(3);
                        note.description = result.getString(4);
                        note.category = result.getString(5);
                        note.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        note.createBy = result.getInt(7);
                        note.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        note.updateBy = result.getInt(9);
                        note.userDetails = new ArrayList<>();
                        note.userDetails.add(new UserDetail(result.getInt(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), result.getString(14), (String[]) result.getArray(15).getArray()));

                        groupNotes.add(note);
                    }

                    stmt = con
                            .prepareStatement("SELECT c1.id,cyclemeetingid,(note).title title,(note).description description ," +
                                    " (note).category category, c1.createdon ," +
                                    " c1.createdby, c1.updateon, c1.updateby, u.username,u.firstname,u.lastname, " +
                                    " (uf.address).city city, (uf.address).state state,(uf.address).phone phone" +
                                    "  FROM (SELECT * FROM " + schemaName + ".cycleMeetingNotes c1 WHERE c1.createdby = ?)c1 " +
                                    " left join master.users u on u.id = createdby " +
                                    " left join " + schemaName + ".userprofile uf ON uf.userid = c1.updateby " +
                                    " ORDER BY c1.createdon DESC ");
                    stmt.setInt(1, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Note note = new Note();
                        note.id = result.getInt(1);
                        note.cycleMeetingId = result.getInt(2);
                        note.title = result.getString(3);
                        note.description = result.getString(4);
                        note.category = result.getString(5);
                        note.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        note.createBy = result.getInt(7);
                        note.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        note.updateBy = result.getInt(9);
                        note.userDetails = new ArrayList<>();
                        note.userDetails.add(new UserDetail(result.getInt(9), result.getString(10), result.getString(11), result.getString(12), result.getString(13), result.getString(14), (String[]) result.getArray(15).getArray()));

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
        } else {
            throw new NotAuthorizedException("");
        }
    }
}

