package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeUtils;

import javax.ws.rs.BadRequestException;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lcom62_one on 1/16/2017.
 */
public class FeedSchedule {

    @JsonView({UserViews.feedScheduleView.class, UserViews.feedDeliveryView.class})
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

    @JsonView({UserViews.feedScheduleView.class, UserViews.feedDeliveryView.class})
    @JsonProperty("feedName")
    public String feedName;

    @JsonView({UserViews.feedDeliveryView.class})
    @JsonProperty("feedDesc")
    public String feedDesc;

    @JsonView({UserViews.feedScheduleView.class, UserViews.feedDeliveryView.class})
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

    @JsonView({UserViews.feedDeliveryView.class})
    @JsonProperty("deliveryTime")
    public String deliveryTime;

    @JsonView(UserViews.feedDeliveryView.class)
    @JsonProperty("divName")
    public String divName;

    @JsonView({UserViews.feedDeliveryView.class, UserViews.deliveredFeedsView.class})
    @JsonProperty("pillId")
    public int pillId;

    @JsonView({UserViews.feedDeliveryView.class, UserViews.deliveredFeedsView.class})
    @JsonProperty("pillName")
    public String pillName;

    @JsonView(UserViews.deliveredFeedsView.class)
    @JsonProperty("pillBody")
    public String pillBody;

    @JsonView(UserViews.feedScheduleView.class)
    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonView(UserViews.deliveredFeedsView.class)
    @JsonProperty("deliveredCount")
    public int deliveredCount;

    @JsonView(UserViews.deliveredFeedsView.class)
    @JsonProperty("failureCount")
    public int failureCount;


    // make the default constructor visible to package only.
    public FeedSchedule() {
    }

    public static final int Feed = 19;


    /***
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
                            " FROM " + schemaName + ".feedschedule fs" +
                            " left join " + schemaName + ".feeds f on f.id = fs.feedid " +
                            " left join master.users u on u.id = fs.createby " +
                            " left join " + schemaName + ".userprofile uf on uf.userid = fs.createby " +
                            " WHERE feedid = ? ORDER BY fs.createdate DESC ");
                    stmt.setInt(1, feedId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        FeedSchedule feedSchedule = new FeedSchedule();
                        feedSchedule.id = resultSet.getInt(1);
                        feedSchedule.territories = (Integer[]) resultSet.getArray(2).getArray();
                        feedSchedule.feedStartDate = resultSet.getTimestamp(3);
                        feedSchedule.feedEndDate = resultSet.getTimestamp(4);
                        feedSchedule.rotate = resultSet.getBoolean(5);
                        feedSchedule.createdate = resultSet.getTimestamp(6);
                        feedSchedule.createby = resultSet.getInt(7);
                        feedSchedule.feedName = resultSet.getString(13);
                        if (resultSet.getArray(12) != null) {
                            Integer[] users = (Integer[]) resultSet.getArray(12).getArray();
                            feedSchedule.participants = users.length;
                        }

                        Date today = new Date();
                        Date startdate = feedSchedule.feedStartDate;
                        Date enddate = feedSchedule.feedEndDate;
                        Date temp = new Date();
                        temp.setDate(startdate.getDate() - 1);

                        boolean isAfter = DateTimeComparator.getDateOnlyInstance().compare(DateTime.now(), enddate) > 0;
                        boolean isBefore = DateTimeComparator.getDateOnlyInstance().compare(startdate, DateTime.now()) > 0;
                        if (isAfter) {
                            feedSchedule.status = "Past";
                        } else if (isBefore) {
                            feedSchedule.status = "Future";
                        } else {
                            feedSchedule.status = "Current";
                        }

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
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<FeedSchedule> recentlyDeliveredPills(int divId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Read") ||
                Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<FeedSchedule> deliveredPillsList = new ArrayList<>();
            ResultSet resultSet = null;
            ResultSet feedSet = null;
            String schemaName = loggedInUser.schemaName;


            try {
                if (con != null) {

                    if (loggedInUser.roles.get(0).roleName.equals("ROOT")) {
                        stmt = con.prepareStatement(" SELECT fd1.id,feed, pillid, deliverytime , " +
                                " (SELECT count(fd.userid) from " + schemaName + ".feeddelivery fd where fd.pillid = fd1.pillid GROUP BY fd.pillid) as Total " +
                                " FROM " + schemaName + ".feeddelivery fd1 " +
                                " left join " + schemaName + ".feedschedule fs on fs.id = fd1.feed" +
                                " left join " + schemaName + ".feeds f on f.id = fs.feedid " +
                                " WHERE f.divid = ? " +
                                " GROUP BY fd1.id,pillid,feed,deliverytime " +
                                " ORDER BY fd1.createdate DESC " +
                                " LIMIT 10");
                        stmt.setInt(1, divId);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            FeedSchedule deliverFeed = new FeedSchedule();
                            deliverFeed.id = resultSet.getInt(1);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                            String date = sdf.format(resultSet.getTimestamp(4));

                            deliverFeed.deliveryTime = date;
                            deliverFeed.participants = resultSet.getInt("Total");
                            deliverFeed.pillId = resultSet.getInt(3);

                            stmt = con.prepareStatement(" SELECT d.name,f.name,f.description,p.title " +
                                    " FROM " + schemaName + ".feedschedule fs " +
                                    " LEFT JOIN " + schemaName + ".feeds f on fs.feedid = f.id " +
                                    " LEFT JOIN " + schemaName + ".pills p on p.id = ? " +
                                    " LEFT JOIN " + schemaName + ".divisions d on d.id = f.divid " +
                                    " WHERE fs.id= ? ");
                            stmt.setInt(1, resultSet.getInt(3));
                            stmt.setInt(2, resultSet.getInt(2));
                            feedSet = stmt.executeQuery();
                            while (feedSet.next()) {
                                deliverFeed.divName = feedSet.getString(1);
                                deliverFeed.feedName = feedSet.getString(2);
                                deliverFeed.feedDesc = feedSet.getString(3);
                                deliverFeed.pillName = feedSet.getString(4);
                            }
                            deliveredPillsList.add(deliverFeed);
                        }
                    } else if (loggedInUser.roles.get(0).roleName.equals("MARKETING REPRESENTATIVE")) {
                        stmt = con.prepareStatement(" SELECT fd.id,feed, pillid, deliverytime " +
                                " FROM " + schemaName + ".feeddelivery fd " +
                                " left join " + schemaName + ".feedschedule fs on fs.id = fd.feed" +
                                " left join " + schemaName + ".feeds f on f.id = fs.feedid " +
                                " WHERE fd.userid = ? AND f.id = ? " +
                                " ORDER by fd.createdate DESC " +
                                " LIMIT 10 ");
                        stmt.setInt(1, loggedInUser.id);
                        stmt.setInt(2, divId);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            FeedSchedule deliverFeed = new FeedSchedule();
                            deliverFeed.id = resultSet.getInt(1);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                            String date = sdf.format(resultSet.getTimestamp(4));
                            deliverFeed.deliveryTime = date;

                            stmt = con.prepareStatement(" SELECT d.name,f.name,f.description,p.title " +
                                    " FROM " + schemaName + ".feedschedule fs " +
                                    " LEFT JOIN " + schemaName + ".feeds f on fs.feedid = f.id " +
                                    " LEFT JOIN " + schemaName + ".pills p on p.id = ? " +
                                    " LEFT JOIN " + schemaName + ".divisions d on d.id = f.divid " +
                                    " WHERE fs.id= ? ");
                            stmt.setInt(1, resultSet.getInt(3));
                            stmt.setInt(2, resultSet.getInt(2));
                            feedSet = stmt.executeQuery();
                            while (feedSet.next()) {
                                deliverFeed.divName = feedSet.getString(1);
                                deliverFeed.feedName = feedSet.getString(2);
                                deliverFeed.feedDesc = feedSet.getString(3);
                                deliverFeed.pillName = feedSet.getString(4);
                            }
                            deliveredPillsList.add(deliverFeed);
                        }
                    } else if (loggedInUser.roles.get(0).roleName.equals("MANAGEMENT")) {

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
            return deliveredPillsList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
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
            Integer[] pills = new Integer[0];

            try {

                con.setAutoCommit(false);

                stmt = con.prepareStatement(" SELECT pills FROM " + schemaName + ".feeds WHERE id = ? ");
                stmt.setInt(1, node.get("feedId").asInt());
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    pills = (Integer[]) resultSet.getArray(1).getArray();
                }

                if (pills.length > 0) {
                    Integer terr[] = new Integer[node.withArray("territories").size()];

                    List<Integer> userList = new ArrayList<>();

                    for (int i = 0; i < node.withArray("territories").size(); i++) {
                        terr[i] = node.withArray("territories").get(i).asInt();
                    }
                    Array terrArr = con.createArrayOf("int", terr);

                    stmt = con.prepareStatement(" SELECT userid from " + schemaName + ".userterritorymap " +
                            " WHERE terrid = ? ");
                    for (int i = 0; i < terr.length; i++) {
                        stmt.setInt(1, terr[i]);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            System.out.println(" In While ..");

                            if (resultSet.getInt(1) > 0)
                                userList.add(resultSet.getInt(1));

                            System.out.println("Id : " + resultSet.getInt(1));
                        }
                    }

                    Integer userId[] = new Integer[userList.size()];
                    for (int i = 0; i < userList.size(); i++) {
                        userId[i] = userList.get(i);
                    }
                    Array userIdArr = con.createArrayOf("int", userId);
                    System.out.println(" User Ids  : " + userList.size());


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
                    stmt.setTimestamp(2, Timestamp.valueOf(node.get("startDate").asText()));
                    stmt.setTimestamp(3, Timestamp.valueOf(node.get("endDate").asText()));
                    stmt.setBoolean(4, node.get("rotate").asBoolean());
                    stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
                    stmt.setInt(6, loggedInUser.id);
                    stmt.setArray(7, startTimeArr);
                    stmt.setArray(8, endTimeArr);
                    stmt.setArray(9, pillsArr);
                    stmt.setArray(10, userIdArr);
                    stmt.setInt(11, node.get("feedId").asInt());

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

                    con.commit();
                    try {
                            /*https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/*/
                        URL url = new URL("http://34.231.6.39:3000/api/tasks");
//                        URL url = new URL("http://192.168.200.62:3000/api/tasks");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        //String input = "{\"pillID\":\"100\",\"name\":\"iPad 4\"}";
                        String input = "{\"feedScheduleId\":" + feedScheduleId + ",\"schema\":" + "\"" + schemaName + "\"}";

                        OutputStream os = conn.getOutputStream();
                        os.write(input.getBytes());
                        os.flush();
                        if (conn.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
                        }

                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                (conn.getInputStream())));
                        System.out.println(" Response Message : " + conn.getResponseMessage());

                        String output;
                        System.out.println("Output from Server .... \n");
                        while ((output = br.readLine()) != null) {
                            System.out.println(output);
                            ObjectMapper mapper = new ObjectMapper();

                            JsonNode jsonNode = mapper.readTree(output);
                            Integer[] jobIds = new Integer[jsonNode.withArray("jobIds").size()];

                            for (int i = 0; i < jobIds.length; i++) {
                                jobIds[i] = jsonNode.withArray("jobIds").get(i).asInt();
                                System.out.println("JOBS : " + jobIds[i]);
                            }

                            Array jobs = con.createArrayOf("int", jobIds);

                            stmt = con.prepareStatement(" UPDATE " + schemaName + ".feedschedule SET jobids = ? WHERE id = ? ");
                            stmt.setArray(1, jobs);
                            stmt.setInt(2, feedScheduleId);
                            int affectedRows = stmt.executeUpdate();
                            System.out.println(" Affected Rows : " + affectedRows);
                            con.commit();
                        }
                        conn.disconnect();

                    } catch (MalformedURLException e) {

                        e.printStackTrace();

                    } catch (IOException e) {

                        e.printStackTrace();

                    }
                    return feedScheduleId;
                } else {
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
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

   /* public static String newTimes(String newTime, long diff) {
        java.sql.Time myTime1 = java.sql.Time.valueOf(newTime);
        LocalTime localtime1 = myTime1.toLocalTime();
        localtime1 = localtime1.plusMinutes(diff);
        String output1 = localtime1.toString();
        System.out.println("output1 = " + output1);
        String returnTime = output1 + ":00";
        return returnTime;
    }*/

    /***
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
            ResultSet resultSet = null;

            try {
                Integer terr[] = new Integer[node.withArray("territories").size()];
                Integer userId[] = new Integer[node.withArray("territories").size()];
                for (int i = 0; i < node.withArray("territories").size(); i++) {
                    terr[i] = node.withArray("territories").get(i).asInt();
                }
                Array terrArr = con.createArrayOf("int", terr);

                stmt = con.prepareStatement(" SELECT userid from " + schemaName + ".userterritorymap " +
                        " WHERE terrid = ? ");
                for (int i = 0; i < terr.length; i++) {
                    stmt.setInt(1, terr[i]);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        System.out.println(" In While ..");
                        userId[i] = resultSet.getInt(1);
                        System.out.println("Id : " + resultSet.getInt(1));
                    }
                }
                Array userIdArr = con.createArrayOf("int", userId);
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

                stmt = con.prepareStatement(" UPDATE " + schemaName + ".feedschedule " +
                        " SET territories=?, feedstartdate=?, feedenddate=?, rotate=?, userid = ?," +
                        " feedstarttime = ? ,feedendtime = ?,numberofpillperday = ? " +
                        " WHERE id = ? ");
                stmt.setArray(1, terrArr);
                stmt.setTimestamp(2, Timestamp.valueOf(node.get("startDate").asText()));
                stmt.setTimestamp(3, Timestamp.valueOf(node.get("endDate").asText()));
                stmt.setBoolean(4, node.get("rotate").asBoolean());
                stmt.setArray(5, userIdArr);
                stmt.setArray(6, startTimeArr);
                stmt.setArray(7, endTimeArr);
                stmt.setArray(8, pillsArr);
                stmt.setInt(9, node.get("id").asInt());
                affectedRow = stmt.executeUpdate();
                try {
                    /*https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/*/
                    URL url = new URL("http://34.231.6.39:3000/api/tasks");
//                  URL url = new URL("http://192.168.200.11:3010/api/tasks");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    //String input = "{\"pillID\":\"100\",\"name\":\"iPad 4\"}";
                    String input = "{\"feedScheduleId\":" + node.get("id").asInt() + ",\"schema\":" + "\"" + schemaName + "\"}";

                    OutputStream os = conn.getOutputStream();
                    os.write(input.getBytes());
                    os.flush();
                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));
                    System.out.println(" Response Message : " + conn.getResponseMessage());
                    String output;
                    System.out.println("Output from Server .... \n");
                    while ((output = br.readLine()) != null) {
                        System.out.println(output);

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(output);
                        Integer[] jobIds = new Integer[jsonNode.withArray("jobIds").size()];

                        for (int i = 0; i < jobIds.length; i++) {
                            jobIds[i] = jsonNode.withArray("jobIds").get(i).asInt();
                            System.out.println("JOBS : " + jobIds[i]);
                        }

                        Array jobs = con.createArrayOf("int", jobIds);

                        stmt = con.prepareStatement(" UPDATE " + schemaName + ".feedschedule SET jobids = ? WHERE id = ? ");
                        stmt.setArray(1, jobs);
                        stmt.setInt(2, node.get("id").asInt());
                        stmt.executeUpdate();

                    }
                    conn.disconnect();
                } catch (MalformedURLException e) {

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();

                }
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
            ResultSet resultSet = null;
            String jobIds = "";
            int length = 0;

            try {
                stmt = con.prepareStatement(" SELECT jobids FROM " + schemaName + ".feedschedule WHERE id = ? ");
                stmt.setInt(1, id);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    Integer[] arr;

                    if (resultSet.getArray(1).getArray() != null) {
                        arr = (Integer[]) resultSet.getArray(1).getArray();

                        length = arr.length;

                        String a = Arrays.toString(arr); //toString the List or Vector
                        String strArr[] = a.substring(1, a.length() - 1).split(", ");

                        if (strArr.length > 0) {
                            StringBuilder nameBuilder = new StringBuilder();

                            for (String n : strArr) {
                                nameBuilder.append(n.replace("'", "\\")).append(",");
                            }

                            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

                            jobIds = nameBuilder.toString();
                            System.out.println(" JObIds : " + nameBuilder.toString());
                        }
                    }
                }

                stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".feedschedule WHERE id = ? ");
                stmt.setInt(1, id);
                affectedRow = stmt.executeUpdate();

                if (length > 0) {
                    try {
                            /*https://www.mkyong.com/webservices/jax-rs/restfull-java-client-with-java-net-url/*/
                        URL url = new URL("http://34.231.6.39:3000/api/removetasks");
//                        URL url = new URL("http://192.168.200.62:3000/api/removetasks");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        String input = "{\"jobIds\":" + "\"" + jobIds + "\"}";

                        OutputStream os = conn.getOutputStream();
                        os.write(input.getBytes());
                        os.flush();
                        if (conn.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
                        }

                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                (conn.getInputStream())));
                        System.out.println(" Response Message : " + conn.getResponseMessage());


                        String output;
                        System.out.println("Output from Server .... \n");
                        while ((output = br.readLine()) != null) {
                            System.out.println(output);
                        }
                        conn.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
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
     * Method is used to get all details about delivered and failed pills.
     *
     * @param feedScheduleId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<FeedSchedule> getDeliveredData(int feedScheduleId, String status, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Read") ||
                Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            List<FeedSchedule> deliveredFeedList = new ArrayList<>();
            ResultSet resultSet = null;
            ResultSet countSet = null;
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;

            try {
                if (status.equalsIgnoreCase("Future")) {
                    stmt = con.prepareStatement(" SELECT p.id,p.title,p.body FROM " + schemaName + ".pills p " +
                            " where p.id = ANY((SELECT DISTINCT f.pills FROM " + schemaName + ".feeddelivery fd " +
                            " LEFT JOIN " + schemaName + ".feedschedule fs on fs.id = fd.feed " +
                            " LEFT JOIN " + schemaName + ".feeds f on f.id = fs.feedid " +
                            " WHERE fs.id = ?) :: int[]) ");
                    stmt.setInt(1, feedScheduleId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        FeedSchedule feedSchedule = new FeedSchedule();
                        feedSchedule.pillId = resultSet.getInt(1);
                        feedSchedule.pillName = resultSet.getString(2);
                        feedSchedule.pillBody = resultSet.getString(3);

                        deliveredFeedList.add(feedSchedule);
                    }
                } else {
                    stmt = con.prepareStatement(" SELECT p.id,p.title,p.body FROM " + schemaName + ".pills p " +
                            " where p.id = ANY((SELECT DISTINCT f.pills FROM " + schemaName + ".feeddelivery fd " +
                            " LEFT JOIN " + schemaName + ".feedschedule fs on fs.id = fd.feed " +
                            " LEFT JOIN " + schemaName + ".feeds f on f.id = fs.feedid " +
                            " WHERE fs.id = ?) :: int[]) ");
                    stmt.setInt(1, feedScheduleId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        FeedSchedule feedSchedule = new FeedSchedule();
                        feedSchedule.pillId = resultSet.getInt(1);
                        feedSchedule.pillName = resultSet.getString(2);
                        feedSchedule.pillBody = resultSet.getString(3);

                        stmt = con.prepareStatement(" SELECT count(*) As Count FROM " + schemaName + ".feeddelivery " +
                                " where pillid = ? AND feed = ?");
                        stmt.setInt(1, feedSchedule.pillId);
                        stmt.setInt(2, feedScheduleId);
                        countSet = stmt.executeQuery();
                        while (countSet.next()) {
                            feedSchedule.deliveredCount = countSet.getInt("Count");
                        }

                        stmt = con.prepareStatement(" SELECT count(*) As Count FROM " + schemaName + ".feeddeliveryfail " +
                                " where pillid = ? AND feed = ?");
                        stmt.setInt(1, feedSchedule.pillId);
                        stmt.setInt(2, feedScheduleId);
                        countSet = stmt.executeQuery();
                        while (countSet.next()) {
                            feedSchedule.failureCount = countSet.getInt("Count");
                        }

                        deliveredFeedList.add(feedSchedule);
                    }
                }
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
                if (countSet != null)
                    if (!countSet.isClosed())
                        countSet.close();
            }
            return deliveredFeedList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Used to update pill Read time
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updatePillReadTime(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Feed).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;
            int affectedRows = 0;
            try {
                if (con != null) {
                    stmt = con.prepareStatement(" UPDATE " + schemaName + ".feeddelivery SET readtime = ?,answertime = ?,answerjson = ? " +
                            " WHERE userid = ? AND pillid = ? ");
                    stmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
                    stmt.setTimestamp(2, new Timestamp((new Date()).getTime()));
                    stmt.setString(3, node.get("answerJson").asText());
                    stmt.setInt(4, loggedInUser.id);
                    stmt.setInt(5, node.get("pillId").asInt());
                    affectedRows = stmt.executeUpdate();
                } else
                    throw new SQLException("DB Connection is null");
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return affectedRows;
        } else {
            throw new NotAuthorizedException("");
        }
    }
}
