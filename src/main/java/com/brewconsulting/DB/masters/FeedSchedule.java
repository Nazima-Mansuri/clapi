package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lcom62_one on 1/16/2017.
 */
public class FeedSchedule {

    @JsonView({UserViews.feedScheduleView.class,UserViews.feedDeliveryView.class})
    @JsonProperty("id")
    public int id;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("territories")
    public Integer[] territories;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("feedStartDate")
    public Date feedStartDate;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("feedEndDate")
    public Date feedEndDate;

    @JsonView({UserViews.feedScheduleView.class,UserViews.feedDeliveryView.class})
    @JsonProperty("feedName")
    public String feedName;

    @JsonView({UserViews.feedDeliveryView.class})
    @JsonProperty("feedDesc")
    public String feedDesc;

    @JsonView({UserViews.feedScheduleView.class,UserViews.feedDeliveryView.class})
    @JsonProperty("participants")
    public int participants;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("status")
    public String status;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("rotate")
    public boolean rotate;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("feedStartTime")
    public Time[] feedStartTime;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("feedEndTime")
    public Time[] feedEndTime;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("numberOfPillsPerDay")
    public Integer[] numberOfPillsPerDay;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdate")
    public Date createdate;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("createby")
    public int createby;

    @JsonView(UserViews.feedDeliveryView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("deliveryTime")
    public Date deliveryTime;

    @JsonView(UserViews.feedDeliveryView.class)
    @JsonProperty("divName")
    public String divName;

    @JsonView(UserViews.feedDeliveryView.class)
    @JsonProperty("pillName")
    public String pillName;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;



    // make the default constructor visible to package only.
    public FeedSchedule() {
    }

    public static final int Feed = 19;


    /***
     *
     * @param feedId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<FeedSchedule> getAllFeedSchedule(int feedId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Read") ||
                Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<FeedSchedule> feedScheduleList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet = null;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT fs.id, territories, feedstartdate, feedenddate, rotate, fs.createdate," +
                            " fs.createby, feedstarttime, feedendtime, numberofpillperday, feedid, " +
                            " fs.userid,f.name,u.username,u.firstname,u.lastname,(uf.address).city," +
                            " (uf.address).state,(uf.address).phone" +
                            " FROM "+schemaName+".feedschedule fs" +
                            " left join "+schemaName+".feeds f on f.id = fs.feedid " +
                            " left join master.users u on u.id = fs.createby " +
                            " left join "+schemaName+".userprofile uf on uf.userid = fs.createby " +
                            " WHERE feedid = ? ");
                    stmt.setInt(1, feedId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        FeedSchedule feedSchedule = new FeedSchedule();
                        feedSchedule.id = resultSet.getInt(1);
                        feedSchedule.territories = (Integer[]) resultSet.getArray(2).getArray();
                        feedSchedule.feedStartDate = resultSet.getTimestamp(3);
                        feedSchedule.feedEndDate = resultSet.getTimestamp(4);
                        feedSchedule.rotate = resultSet.getBoolean(5);
                        feedSchedule.feedName = resultSet.getString(13);
                        Integer[] users = (Integer[]) resultSet.getArray(12).getArray();
                        feedSchedule.participants = users.length;
                        if(feedSchedule.feedEndDate.before(new Date()) && !feedSchedule.feedEndDate.equals(new Date()))
                            feedSchedule.status = "Past";
                        else if(feedSchedule.feedStartDate.after(new Date()) && feedSchedule.feedEndDate.after(new Date()) && !feedSchedule.feedEndDate.equals(new Date()))
                            feedSchedule.status = "Future";
                        else
                            feedSchedule.status = "Current";

                        feedSchedule.feedStartTime = (Time[]) resultSet.getArray(8).getArray();
                        feedSchedule.feedEndTime = (Time[]) resultSet.getArray(9).getArray();
                        feedSchedule.numberOfPillsPerDay = (Integer[]) resultSet.getArray(10).getArray();
                        feedSchedule.userDetails = new ArrayList<>();
                        feedSchedule.userDetails.add(new UserDetail(
                                resultSet.getInt(7),
                                resultSet.getString(14),
                                resultSet.getString(15),
                                resultSet.getString(16),
                                resultSet.getString(17),
                                resultSet.getString(18),
                                (String[]) resultSet.getArray(19).getArray()

                        ));
                        feedScheduleList.add(feedSchedule);
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
            return feedScheduleList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<FeedSchedule> recentlyDeliveredPills(LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Feed).equals("Read") ||
                Permissions.isAuthorised(userRole,Feed).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<FeedSchedule> deliveredPillsList = new ArrayList<>();
            ResultSet resultSet = null;
            ResultSet feedSet = null;
            String schemaName = loggedInUser.schemaName;


            try
            {
                if(con != null)
                {

                    if(loggedInUser.roles.get(0).roleName.equals("ROOT"))
                    {
                        stmt = con.prepareStatement(" SELECT fd1.id,feed, pillid, deliverytime , " +
                                " (SELECT count(fd.userid) from "+schemaName+".feeddelivery fd where fd.pillid = fd1.pillid GROUP BY fd.pillid) as Total " +
                                " FROM "+schemaName+".feeddelivery fd1 GROUP BY fd1.id,pillid,feed,deliverytime");
                        resultSet = stmt.executeQuery();
                        while (resultSet.next())
                        {
                            FeedSchedule deliverFeed = new FeedSchedule();
                            deliverFeed.id = resultSet.getInt(1);
                            deliverFeed.deliveryTime = resultSet.getTimestamp(4);
                            deliverFeed.participants = resultSet.getInt("Total");

                            stmt = con.prepareStatement(" SELECT d.name,f.name,f.description,p.title " +
                                    " FROM "+schemaName+".feedschedule fs " +
                                    " LEFT JOIN "+schemaName+".feeds f on fs.feedid = f.id " +
                                    " LEFT JOIN "+schemaName+".pills p on p.id = ? " +
                                    " LEFT JOIN "+schemaName+".divisions d on d.id = f.divid " +
                                    " WHERE fs.id= ? ");
                            stmt.setInt(1,resultSet.getInt(3));
                            stmt.setInt(2,resultSet.getInt(2));
                            feedSet = stmt.executeQuery();
                            while (feedSet.next())
                            {
                                deliverFeed.divName = feedSet.getString(1);
                                deliverFeed.feedName = feedSet.getString(2);
                                deliverFeed.feedDesc = feedSet.getString(3);
                                deliverFeed.pillName = feedSet.getString(4);
                            }
                            deliveredPillsList.add(deliverFeed);
                        }
                    }
                    else if(loggedInUser.roles.get(0).roleName.equals("MARKETING REPRESENTATIVE"))
                    {
                        stmt = con.prepareStatement(" SELECT id,feed, pillid, deliverytime " +
                                " FROM "+schemaName+".feeddelivery fd " +
                                " WHERE fd.userid = ? ");
                        stmt.setInt(1,loggedInUser.id);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next())
                        {
                            FeedSchedule deliverFeed = new FeedSchedule();
                            deliverFeed.id = resultSet.getInt(1);
                            deliverFeed.deliveryTime = resultSet.getTimestamp(4);

                            stmt = con.prepareStatement(" SELECT d.name,f.name,f.description,p.title " +
                                    " FROM "+schemaName+".feedschedule fs " +
                                    " LEFT JOIN "+schemaName+".feeds f on fs.feedid = f.id " +
                                    " LEFT JOIN "+schemaName+".pills p on p.id = ? " +
                                    " LEFT JOIN "+schemaName+".divisions d on d.id = f.divid " +
                                    " WHERE fs.id= ? ");
                            stmt.setInt(1,resultSet.getInt(3));
                            stmt.setInt(2,resultSet.getInt(2));
                            feedSet = stmt.executeQuery();
                            while (feedSet.next())
                            {
                                deliverFeed.divName = feedSet.getString(1);
                                deliverFeed.feedName = feedSet.getString(2);
                                deliverFeed.feedDesc = feedSet.getString(3);
                                deliverFeed.pillName = feedSet.getString(4);
                            }
                            deliveredPillsList.add(deliverFeed);
                        }
                    }
                    else if(loggedInUser.roles.get(0).roleName.equals("MANAGEMENT"))
                    {

                    }


                }
                else
                    throw new Exception("DB connection is null");
            }
            finally {
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
            return deliveredPillsList;
        }
        else
        {
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
    public static int addFeedSchedule(JsonNode node, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet = null;
            String schemaName = loggedInUser.schemaName;

            try {
                con.setAutoCommit(false);

                Integer terr[] = new Integer[node.withArray("territories").size()];
                Integer userId[] = new Integer[node.withArray("territories").size()];
                for (int i = 0; i < node.withArray("territories").size(); i++) {
                    terr[i] = node.withArray("territories").get(i).asInt();
                }
                Array terrArr = con.createArrayOf("int", terr);

                stmt = con.prepareStatement(" SELECT userid from "+schemaName+".userterritorymap " +
                        " WHERE terrid = ? ");
                for (int i=0;i<terr.length;i++)
                {
                    stmt.setInt(1,terr[i]);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()){
                        System.out.println(" In While ..");
                        userId[i] = resultSet.getInt(1);
                        System.out.println("Id : " + resultSet.getInt(1));
                    }
                }
                Array userIdArr = con.createArrayOf("int",userId);
                System.out.println(" User Ids  : " + userId.length);


                Time[] startTime = new Time[node.withArray("feedStartTime").size()];
                for (int i = 0; i < node.withArray("feedStartTime").size(); i++) {
                    startTime[i] = Time.valueOf(node.withArray("feedStartTime").get(i).asText());
                }
                Array startTimeArr = con.createArrayOf("time", startTime);

                Time[] endTime = new Time[node.withArray("feedEndTime").size()];
                for (int i = 0; i < node.withArray("feedEndTime").size(); i++) {
                    endTime[i] = Time.valueOf(node.withArray("feedEndTime").get(i).asText());
                }
                Array endTimeArr = con.createArrayOf("time", endTime);

                Integer noOfPills[] = new Integer[node.withArray("noOfPillsPerDay").size()];
                for (int i = 0; i < node.withArray("noOfPillsPerDay").size(); i++) {
                    noOfPills[i] = node.withArray("noOfPillsPerDay").get(i).asInt();
                }
                Array pillsArr = con.createArrayOf("int", noOfPills);

                stmt = con.prepareStatement(" INSERT INTO " + schemaName
                        + ".feedschedule(territories, feedstartdate, feedenddate, rotate, createdate, createby,"
                        + " feedstarttime,feedendtime,numberofpillperday,userid,feedid)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                stmt.setArray(1, terrArr);
                stmt.setDate(2, java.sql.Date.valueOf(node.get("startDate").asText()));
                stmt.setDate(3, java.sql.Date.valueOf(node.get("endDate").asText()));
                stmt.setBoolean(4, node.get("rotate").asBoolean());
                stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                stmt.setInt(6, loggedInUser.id);
                stmt.setArray(7, startTimeArr);
                stmt.setArray(8, endTimeArr);
                stmt.setArray(9, pillsArr);
                stmt.setArray(10,userIdArr);
                stmt.setInt(11,node.get("feedId").asInt());


                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Feeds Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int feedScheduleId;
                if (generatedKeys.next())
                    // It gives last inserted Id in questionId
                    feedScheduleId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

//                try {
//                            /*https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/*/
//                    URL url = new URL("http://localhost:3000/api/tasks");
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setDoOutput(true);
//                    conn.setRequestMethod("POST");
//                    conn.setRequestProperty("Content-Type", "application/json");
//                    int ud = 10;
//                    //String input = "{\"pillID\":\"100\",\"name\":\"iPad 4\"}";
//                    String input = "{\"feedScheduleId\":"+ud+"}";
//                    OutputStream os = conn.getOutputStream();
//                    os.write(input.getBytes());
//                    os.flush();
//                    if (conn.getResponseCode() != 200) {
//                        throw new RuntimeException("Failed : HTTP error code : "
//                                + conn.getResponseCode());
//                    }
//
//                    BufferedReader br = new BufferedReader(new InputStreamReader(
//                            (conn.getInputStream())));
//
//                    String output;
//                    System.out.println("Output from Server .... \n");
//                    while ((output = br.readLine()) != null) {
//                        System.out.println(output);
//                    }
//
//                    conn.disconnect();
//
//                } catch (MalformedURLException e) {
//
//                    e.printStackTrace();
//
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//
//                }
                con.commit();
                return feedScheduleId;
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

    public static String newTimes(String newTime, long diff) {
        java.sql.Time myTime1 = java.sql.Time.valueOf(newTime);
        LocalTime localtime1 = myTime1.toLocalTime();
        localtime1 = localtime1.plusMinutes(diff);
        String output1 = localtime1.toString();
        System.out.println("output1 = " + output1);
        String returnTime = output1 + ":00";
        return returnTime;
    }

    /***
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateFeedSchedule(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                Integer terr[] = new Integer[node.withArray("territories").size()];
                for (int i = 0; i < node.withArray("territories").size(); i++) {
                    terr[i] = node.withArray("territories").get(i).asInt();
                }
                Array terrArr = con.createArrayOf("int", terr);

                stmt = con.prepareStatement(" UPDATE " + schemaName + ".feedschedule " +
                        " SET territories=?, deliverfeeds=?, feedstartdate=?, feedenddate=?, rotate=? " +
                        " WHERE id = ? ");
                stmt.setArray(1, terrArr);
                stmt.setArray(2, terrArr);
                stmt.setDate(3, java.sql.Date.valueOf(node.get("startDate").asText()));
                stmt.setDate(4, java.sql.Date.valueOf(node.get("endDate").asText()));
                stmt.setBoolean(5, node.get("rotate").asBoolean());
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
    public static int deleteFeedSchedule(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".feedschedule WHERE id = ? ");
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
}
