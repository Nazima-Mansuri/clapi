package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lcom53 on 1/12/16.
 */
public class GroupPost {

    @JsonProperty("id")
    public int id;

    @JsonProperty("groupId")
    public int groupId;

    @JsonProperty("postText")
    public String postText;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("postProfileImage")
    public String postProfileImage;

    @JsonProperty("comments")
    public ArrayList<GroupPostReply> comments;


    public static List<GroupPost> getGroupPost(int groupId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        GroupPost groupPost = null;
        GroupPostReply groupPostReply = null;
        // TODO check authorization
        String schemaName = loggedInUser.schemaName;
        ArrayList<GroupPost> groupPostList = new ArrayList<GroupPost>();
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            if (con != null) {
                stmt = con
                        .prepareStatement(" SELECT c1.id, c1.groupid, c1.posttext, c1.createdate, c1.createby,c3.username, " +
                                " c3.firstname,c3.lastname, (c4.address).city city,(c4.address).state state,(c4.address).phone phone,c4.profileimage, " +
                                " c2.id,c2.grouppostid, c2.replytext, c2.createdate, c2.createby ,c5.username," +
                                " c5.firstname,c5.lastname, (c6.address).city city,(c6.address).state state,(c6.address).phone phone,c6.profileimage " +
                                " FROM " + schemaName + ".grouppost c1 " +
                                " left join " + schemaName + ".grouppostreply c2 on c1.id = c2.grouppostid " +
                                " left join "+schemaName+".userprofile c4 on c4.userid = c1.createby " +
                                " left join master.users c3 on c3.id = c4.userid " +
                                " left join "+schemaName+".userprofile c6 on c6.userid = c2.createby " +
                                " left join master.users c5 on c5.id = c6.userid " +
                                " WHERE c1.groupid = ? ORDER BY c1.createdate DESC");
                stmt.setInt(1, groupId);
                result = stmt.executeQuery();

                while (result.next()) {
                    groupPost = new GroupPost();
                    groupPostReply = new GroupPostReply();
                    groupPost.comments = new ArrayList<>();
                    groupPost.id = result.getInt(1);
                    groupPost.groupId = result.getInt(2);
                    groupPost.postText = result.getString(3);
                    groupPost.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(4).getTime())));
                    groupPost.createBy = result.getInt(5);
                    groupPost.userDetails = new ArrayList<>();
                    groupPost.userDetails.add(new UserDetail(result.getInt(5),result.getString(6),result.getString(7),result.getString(8),result.getString(9),result.getString(10), (String[]) result.getArray(11).getArray()));
                    groupPost.postProfileImage = result.getString(12);

                    groupPostReply.id = result.getInt(13);
                    if (groupPostReply.id != 0) {
                        groupPostReply.groupPostId = result.getInt(14);
                        groupPostReply.replyText = result.getString(15);
                        groupPostReply.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(16).getTime())));
                        groupPostReply.createBy = result.getInt(17);
                        groupPostReply.userDetails = new ArrayList<>();
                        groupPostReply.userDetails.add(new UserDetail(result.getInt(17),result.getString(18),result.getString(19),result.getString(20),result.getString(21),result.getString(22), (String[]) result.getArray(23).getArray()));
                        groupPostReply.replyProfileImage = result.getString(24);
                    }

                    int index = findPost(groupPost.id, groupPostList);
                    if (index != -1) {
                        groupPostList.get(index).comments.add(groupPostReply);

                    } else {
                        groupPostList.add(groupPost);
                        if (groupPostReply.id != 0)
                            groupPost.comments.add(groupPostReply);
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
        return groupPostList;
    }


    /**
     * Method Add group Post in Database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addGroupPost(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int result;

        try {
            con.setAutoCommit(false);

            stmt = con
                    .prepareStatement(
                            "INSERT INTO "
                                    + schemaName
                                    + ".grouppost(groupid,posttext,createby,createdate) values (?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, node.get("groupid").asInt());
            stmt.setString(2, node.get("posttext").asText());
            stmt.setInt(3, loggedInUser.id);
            stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
            result = stmt.executeUpdate();

            if (result == 0)
                throw new SQLException("Add Group Post Failed.");

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            int id;
            if (generatedKeys.next())
                // It gives last inserted Id in divisionId
                id = generatedKeys.getInt(1);
            else
                throw new SQLException("No ID obtained");

            con.commit();
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
    }

    /**
     * Method used to find post by id.
     *
     * @param postid
     * @param list
     * @return
     */
    public static int findPost(int postid, List<GroupPost> list) {
        for (GroupPost groupPost : list) {
            if (groupPost.id == postid) {
                return list.indexOf(groupPost);
            }
        }
        return -1;
    }

}
