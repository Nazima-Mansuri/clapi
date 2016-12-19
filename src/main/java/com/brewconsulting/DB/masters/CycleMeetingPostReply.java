package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lcom53 on 1/12/16.
 */
public class CycleMeetingPostReply {

    @JsonProperty("id")
    public int id;

    @JsonProperty("cycleMeetingPostId")
    public int cycleMeetingPostId;

    @JsonProperty("replyText")
    public String replyText;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("replyProfileImage")
    public String replyProfileImage;

    /***
     * Method is used to add Cycle meeting post reply.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addMeetingPostReply(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 17).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("INSERT INTO "+schemaName
                        +".cyclemeetingpostreply(cyclemeetingpostid, replytext, createdate, createby)"
                        +" VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1,node.get("cyclemeetingpostid").asInt());
                stmt.setString(2,node.get("replytext").asText());
                stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
                stmt.setInt(4,loggedInUser.id);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Reply Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int meetingPostReplyId;
                if (generatedKeys.next())
                    // It gives last inserted Id in meetingPostReplyId
                    meetingPostReplyId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");
                System.out.println("ID : " + meetingPostReplyId);

                con.commit();
                return meetingPostReplyId;
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
