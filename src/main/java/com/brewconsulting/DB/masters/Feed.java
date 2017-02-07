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
 * Created by lcom62_one on 1/13/2017.
 */
public class Feed {

    @JsonProperty("id")
    public int id;

    @JsonProperty("divid")
    public int divid;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("pills")
    public Integer[] pills;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdate")
    public Date createdate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createby")
    public int createby;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    // make the default constructor visible to package only.
    public Feed() {
    }

    public static final int Feed = 19;

    /***
     *
     * @param divId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Feed> getAllFeeds(int divId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Read") ||
                Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Feed> feedList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet = null;

            try {
                if (con != null) {
                    stmt = con.prepareStatement(" SELECT f.id, f.divid, name, description, pills, f.createdate, f.createby," +
                            " u.username,u.firstname,u.lastname,(uf.address).city,(uf.address).state,(uf.address).phone " +
                            " FROM " + schemaName + ".feeds f " +
                            " left join master.users u on u.id = f.createby " +
                            " left join "+schemaName+".userprofile uf on uf.userid = f.createby " +
                            " WHERE f.divid = ? ORDER BY f.createdate");
                    stmt.setInt(1, divId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        Feed feed = new Feed();
                        feed.id = resultSet.getInt(1);
                        feed.divid = resultSet.getInt(2);
                        feed.name = resultSet.getString(3);
                        feed.description = resultSet.getString(4);
                        feed.pills = (Integer[]) resultSet.getArray(5).getArray();
                        feed.createdate = resultSet.getTimestamp(6);
                        feed.createby = resultSet.getInt(7);
                        feed.userDetails = new ArrayList<>();
                        feed.userDetails.add(new UserDetail(resultSet.getInt(7),resultSet.getString(8),resultSet.getString(9),resultSet.getString(10),resultSet.getString(11),resultSet.getString(12), (String[]) resultSet.getArray(13).getArray()));
                        feedList.add(feed);
                    }
                } else
                    throw new Exception("DB connection is null");
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
            }
            return feedList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addFeeds(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                con.setAutoCommit(false);

                Integer[] feeds = new Integer[0];
                Array feedsArr = con.createArrayOf("int", feeds);

                stmt = con.prepareStatement(" INSERT INTO " + schemaName
                        + ".feeds(divid,name,description,pills,createdate,createby)"
                        + " VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, node.get("divId").asInt());
                stmt.setString(2, node.get("name").asText());
                stmt.setString(3, node.get("description").asText());
                stmt.setArray(4, feedsArr);
                stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                stmt.setInt(6, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Feeds Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int feedId;
                if (generatedKeys.next())
                    // It gives last inserted Id in questionId
                    feedId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return feedId;

            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updatePillsInFeed(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                Integer[] feeds = new Integer[node.withArray("pills").size()];
                for (int i = 0; i < node.withArray("pills").size(); i++) {
                    feeds[i] = node.withArray("pills").get(i).asInt();
                }
                Array feedArr = con.createArrayOf("int", feeds);

                stmt = con.prepareStatement(" UPDATE " + schemaName + ".feeds SET pills = ? " +
                        " WHERE id = ? ");
                stmt.setArray(1, feedArr);
                stmt.setInt(2, node.get("id").asInt());
                affectedRow = stmt.executeUpdate();
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return affectedRow;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteFeeds(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".feeds WHERE id = ? ");
                stmt.setInt(1, id);
                affectedRow = stmt.executeUpdate();
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return affectedRow;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  Method is used to remove pill from feed.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removePillFromFeed(JsonNode node,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Feed).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt =null;
            String schemaName = loggedInUser.schemaName;
            int result =0;

            try
            {
                stmt = con.prepareStatement("UPDATE "
                        + schemaName
                        + ".feeds SET pills = array_remove(pills, ? )"
                        + " WHERE id = ?");
                stmt.setInt(1,node.get("pillId").asInt());
                stmt.setInt(2,node.get("feedId").asInt());
                result = stmt.executeUpdate();

            }
            finally {
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return  result;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}
