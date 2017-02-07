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
import java.util.List;

/**
 * Created by lcom62_one on 1/12/2017.
 */
public class MeetingProcessContent {

    @JsonProperty("id")
    public int id;

    @JsonProperty("meetingId")
    public int meetingId;

    @JsonProperty("agendaId")
    public int agendaId;

    @JsonProperty("contentURL")
    public String contentURL;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createDate")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("updateDate")
    public Date updateDate;

    @JsonProperty("updateBy")
    public int updateBy;

    // make visible to package only.
    public MeetingProcessContent() {
    }

    public static final int Content = 11;


    public static List<MeetingProcessContent> getAllMeetingProcessContent(int meetingId,int agendaId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            String schemaName = loggedInUser.schemaName;
            List<MeetingProcessContent> processContentList = new ArrayList<>();

            try
            {
                if(con != null)
                {
                    stmt = con.prepareStatement(" SELECT id, meetingid, agendaid, contenturl, " +
                            " createdate, createby, updatedate, updateby " +
                            " FROM "+schemaName+".cyclemeetingprocesscontent " +
                            " WHERE meetingid = ? AND agendaid = ? ");
                    stmt.setInt(1,meetingId);
                    stmt.setInt(2,agendaId);
                    result = stmt.executeQuery();
                    while (result.next())
                    {
                        MeetingProcessContent processContent = new MeetingProcessContent();
                        processContent.id = result.getInt(1);
                        processContent.meetingId = result.getInt(2);
                        processContent.agendaId = result.getInt(3);
                        processContent.contentURL = result.getString(4);
                        processContent.createDate = result.getTimestamp(5);
                        processContent.createBy = result.getInt(6);
                        processContent.updateDate = result.getTimestamp(7);
                        processContent.updateBy = result.getInt(8);
                        processContentList.add(processContent);
                    }
                }
                else
                    throw new Exception("DB connection is null");
            }
            finally {
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return processContentList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    public static List<Integer> addMeetingProcessContent(int meetingId,int agendaId,List<String> filePath,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;
            int result =0;
            List<Integer> idList = new ArrayList<>();

            try {
                con.setAutoCommit(false);

                for (int i=0;i<filePath.size();i++)
                {
                    stmt = con.prepareStatement(" INSERT INTO "+schemaName
                            +".cyclemeetingprocesscontent(meetingid, agendaid, contenturl, createdate, createby, updatedate, updateby) "
                            +" VALUES (?, ?, ?, ?, ?, ?, ?) ", Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1,meetingId);
                    stmt.setInt(2,agendaId);
                    stmt.setString(3,filePath.get(i));
                    stmt.setTimestamp(4,new Timestamp((new java.util.Date()).getTime()));
                    stmt.setInt(5,loggedInUser.id);
                    stmt.setTimestamp(6,new Timestamp((new java.util.Date()).getTime()));
                    stmt.setInt(7,loggedInUser.id);

                    result = stmt.executeUpdate();

                    if (result == 0)
                        throw new SQLException("Add meeting process content Failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int processContentId;
                    if (generatedKeys.next())
                        // It gives last inserted Id in quesCollectionId
                        processContentId = generatedKeys.getInt(1);
                    else
                        throw new SQLException("No ID obtained");

                    idList.add(processContentId);
                }

                con.commit();
                return  idList;
            }catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            }
            finally {
                con.setAutoCommit(false);
                if(con != null)
                    if(!con.isClosed())
                        con.close();
            }
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
    
    public static int updateMeetingProcessContent(String url,int id,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;
            int affectedRow = 0;

            try
            {
                if(con != null)
                {
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".cyclemeetingprocesscontent SET contenturl = ? ,updatedate = ?, updateby =? "
                                    + " WHERE id = ?");
                    stmt.setString(1,url);
                    stmt.setTimestamp(2,new Timestamp((new java.util.Date()).getTime()));
                    stmt.setInt(3,loggedInUser.id);
                    stmt.setInt(4,id);
                    affectedRow = stmt.executeUpdate();
                }
                else
                     throw new Exception("DB connection is null");
            }
            finally {
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return affectedRow;
        }
        else 
        {
            throw new NotAuthorizedException("");
        }
    }

    public static int deleteMeetingProcessContent(int id, LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Content).equals("Read") ||
                Permissions.isAuthorised(userRole,Content).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;
            int affectedRows = 0;
            try
            {
                stmt = con.prepareStatement(" DELETE FROM "+schemaName+".cyclemeetingprocesscontent" +
                        " WHERE id = ? ");
                stmt.setInt(1,id);
                affectedRows = stmt.executeUpdate();
            }
            finally {
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return affectedRows;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}
