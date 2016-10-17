package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.Date;

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

    public enum noteCategory
    {
        Prepare_Edit_Content, Venue_Arrangements,Travel_Arrangement,Misc
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

                noteCategory categoryType = noteCategory.valueOf(node.get("category").asText());

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
}
