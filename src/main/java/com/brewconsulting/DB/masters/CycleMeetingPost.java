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
 * Created by lcom53 on 1/12/16.
 */
public class CycleMeetingPost {
    @JsonProperty("id")
    public int id;

    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonProperty("postText")
    public String postText;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("userId")
    public Integer[] userId;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("postProfileImage")
    public String postProfileImage;

    @JsonProperty("comments")
    public ArrayList<CycleMeetingPostReply> comments;


    public static List<CycleMeetingPost> getMeetingPost(int meetingId,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        CycleMeetingPost meetingPost = null;
        CycleMeetingPostReply meetingPostReply = null;
        // TODO check authorization
        String schemaName = loggedInUser.schemaName;
        ArrayList<CycleMeetingPost> meetingPostList = new ArrayList<CycleMeetingPost>();
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            if (con != null) {
                stmt = con
                        .prepareStatement(" SELECT c1.id, c1.cyclemeetingid, c1.posttext, c1.createdate, c1.createby,c1.usertosend,c5.username," +
                                " c5.firstname,c5.lastname,(c3.address).city city,(c3.address).state state,(c3.address).phone phone, c3.profileimage," +
                                " c2.id,c2.cyclemeetingpostid, c2.replytext, c2.createdate, c2.createby ,c6.username,c6.firstname,c6.lastname," +
                                " (c4.address).city city,(c4.address).state state,(c4.address).phone phone, c4.profileimage " +
                                " FROM " + schemaName + ".cyclemeetingpost c1 " +
                                " left join " + schemaName + ".cyclemeetingpostreply c2 on c1.id = c2.cyclemeetingpostid " +
                                " left join "+schemaName+".userprofile c3 on c3.userid = c1.createby " +
                                " left join "+schemaName+".userprofile c4 on c4.userid = c2.createby " +
                                " left join master.users c5 on c5.id = c3.userid " +
                                " left join master.users c6 on c6.id = c4.userid " +
                                " WHERE c1.cyclemeetingid = ? AND (c1.createby = ? OR ? = ANY(c1.usertosend ::int[])) " +
                                " ORDER BY c1.createdate DESC");
                stmt.setInt(1,meetingId);
                stmt.setInt(2, loggedInUser.id);
                stmt.setInt(3, loggedInUser.id);
                result = stmt.executeQuery();

                while (result.next())
                {
                    meetingPost = new CycleMeetingPost();
                    meetingPostReply = new CycleMeetingPostReply();
                    meetingPost.comments = new ArrayList<>();
                    meetingPost.id = result.getInt(1);
                    meetingPost.cycleMeetingId = result.getInt(2);
                    meetingPost.postText = result.getString(3);
                    meetingPost.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(4).getTime())));
                    meetingPost.createBy = result.getInt(5);
                    meetingPost.userId = (Integer[]) result.getArray(6).getArray();
                    meetingPost.userDetails = new ArrayList<>();
                    meetingPost.userDetails.add(new UserDetail(result.getInt(5),result.getString(7),result.getString(8),result.getString(9),result.getString(10),result.getString(11), (String[]) result.getArray(12).getArray()));
                    meetingPost.postProfileImage = result.getString(13);

                    meetingPostReply.id = result.getInt(14);
                    if (meetingPostReply.id != 0) {
                        meetingPostReply.cycleMeetingPostId = result.getInt(15);
                        meetingPostReply.replyText = result.getString(16);
                        meetingPostReply.createDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(17).getTime())));
                        meetingPostReply.createBy = result.getInt(18);
                        meetingPostReply.userDetails = new ArrayList<>();
                        meetingPostReply.userDetails.add(new UserDetail(result.getInt(18),result.getString(19),result.getString(20),result.getString(21),result.getString(22),result.getString(23), (String[]) result.getArray(24).getArray()));
                        meetingPostReply.replyProfileImage = result.getString(25);
                    }

                    int index = findPost(meetingPost.id, meetingPostList);
                    if (index != -1) {
                        meetingPostList.get(index).comments.add(meetingPostReply);

                    } else {
                        meetingPostList.add(meetingPost);
                        if (meetingPostReply.id != 0)
                            meetingPost.comments.add(meetingPostReply);
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
        return meetingPostList;
    }


    /***
     *  Method used to add cyclemeeting post in database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addMeetingPost(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 17).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                Integer[] userIdArr = new Integer[node.withArray("userid").size()];

                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("userid").size(); i++) {
                    userIdArr[i] = node.withArray("userid").get(i).asInt();
                }

                Array userids = con.createArrayOf("int", userIdArr);


                stmt = con.prepareStatement("INSERT INTO "+schemaName
                        +".cyclemeetingpost(cyclemeetingid, posttext, createdate, createby, usertosend)"
                        +" VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1,node.get("cyclemeetingid").asInt());
                stmt.setString(2,node.get("posttext").asText());
                stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                stmt.setInt(4,loggedInUser.id);
                stmt.setArray(5,userids);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Reply Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int meetingPostId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    meetingPostId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");
                System.out.println("ID : " + meetingPostId);

                con.commit();
                return meetingPostId;
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



    public static int findPost(int meetingPostId, List<CycleMeetingPost> list) {
        for (CycleMeetingPost meetingPost : list) {
            if (meetingPost.id == meetingPostId) {
                return list.indexOf(meetingPost);
            }
        }
        return -1;
    }
}
