package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lcom62_one on 2/2/2017.
 */
public class Assesment {

    @JsonView({UserViews.assesmentView.class, UserViews.scoreView.class})
    @JsonProperty("id")
    public int id;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("divId")
    public int divId;

    @JsonView({UserViews.assesmentView.class, UserViews.quesSetView.class, UserViews.scoreView.class})
    @JsonProperty("name")
    public String name;

    @JsonView({UserViews.assesmentView.class, UserViews.scoreView.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonProperty("startDate")
    public java.util.Date startDate;

    @JsonView({UserViews.assesmentView.class, UserViews.scoreView.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonProperty("endDate")
    public java.util.Date endDate;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("users")
    public Integer[] users;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("participants")
    public int participants;

    @JsonView(UserViews.assesmentView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdOn")
    public java.util.Date createdOn;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("createBy")
    public int createBy;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("Instruction")
    public String Instrction;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("EndNote")
    public String EndNote;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("Description")
    public String Description;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("showFeedBack")
    public boolean showFeedBack;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("randomDelivery")
    public boolean randomDelivery;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("AllowReview")
    public boolean AllowReview;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("Scoring")
    public HashMap Scoring;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("TimeLimitation")
    public HashMap TimeLimitation;

    @JsonView(UserViews.quesSetView.class)
    @JsonProperty("questions")
    public List<Question> questions;

    @JsonView(UserViews.scoreView.class)
    @JsonProperty("isExpired")
    public boolean isExpired;

    @JsonView(UserViews.scoreView.class)
    @JsonProperty("score")
    public int score;

    @JsonView(UserViews.assesmentView.class)
    @JsonProperty("status")
    public String status;


    public Assesment() {
    }

    /***
     * Get all Assesment details
     *
     * @param divId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getAllAssesments(int divId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<Assesment> assesmentList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT o.id, startdate, enddate," +
                            " o.userid, o.divid, o.createdon, o.createdby,u.username,u.firstname,u.lastname," +
                            " (uf.address).city,(uf.address).state,(uf.address).phone,assesmentname " +
                            " FROM (SELECT * FROM " + schemaName + ".onthegocontenttest o WHERE divid = ? )o " +
                            " left join master.users u on u.id = o.createdby " +
                            " LEFT JOIN " + schemaName + ".userprofile uf on uf.userid = o.createdby" +
                            " ORDER BY o.createdon DESC");
                    stmt.setInt(1, divId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        Assesment assesment = new Assesment();
                        assesment.id = resultSet.getInt(1);
                        assesment.startDate = resultSet.getTimestamp(2);
                        assesment.endDate = resultSet.getTimestamp(3);
                        assesment.users = (Integer[]) resultSet.getArray(4).getArray();
                        assesment.participants = assesment.users.length;
                        assesment.divId = resultSet.getInt(5);
                        assesment.createdOn = resultSet.getTimestamp(6);
                        assesment.createBy = resultSet.getInt(7);
                        assesment.userDetails = new ArrayList<>();
                        assesment.userDetails.add(new UserDetail(resultSet.getInt(7), resultSet.getString(8), resultSet.getString(9), resultSet.getString(10), resultSet.getString(11), resultSet.getString(12), (String[]) resultSet.getArray(13).getArray()));
                        assesment.name = resultSet.getString(14);

                        java.util.Date startdate = assesment.startDate;
                        java.util.Date enddate = assesment.endDate;
                        java.util.Date temp = new java.util.Date();
                        temp.setDate(startdate.getDate() - 1);

                        boolean isAfter = DateTimeComparator.getDateOnlyInstance().compare(DateTime.now(), enddate) > 0;
                        boolean isBefore = DateTimeComparator.getDateOnlyInstance().compare(startdate, DateTime.now()) > 0;
                        if (isAfter) {
                            assesment.status = "Past";
                        } else if (isBefore) {
                            assesment.status = "Future";
                        } else {
                            assesment.status = "Current";
                        }

                        assesmentList.add(assesment);
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return assesmentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method is used to add OnTheGoAssesment
     *
     * @param loggedInUser
     * @return
     */
    public static int addOnTheGoAssesment(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int result = 0;

            try {
                con.setAutoCommit(false);
                Integer[] territories = new Integer[node.withArray("territories").size()];

                for (int i = 0; i < node.withArray("territories").size(); i++) {
                    territories[i] = node.withArray("territories").get(i).asInt();
                }

                Array terrArr = con.createArrayOf("int", territories);

                Integer[] users = new Integer[node.withArray("territories").size()];
                stmt = con.prepareStatement(" SELECT userid from " + schemaName + ".userterritorymap " +
                        " WHERE terrid = ? ");
                for (int i = 0; i < territories.length; i++) {
                    stmt.setInt(1, territories[i]);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        System.out.println(" In While ..");
                        users[i] = resultSet.getInt(1);
                        System.out.println("Id : " + resultSet.getInt(1));
                    }
                }

                Array userIdArr = con.createArrayOf("int", users);
                System.out.println(" User Ids  : " + users.length);

                Integer[] scorecorr = new Integer[0];
                Array scoreCrArr = con.createArrayOf("int", scorecorr);

                Double[] scoreInCorr = new Double[0];
                Array scoreInCrArr = con.createArrayOf("FLOAt8", scoreInCorr);

                stmt = con.prepareStatement(" INSERT  INTO " + schemaName +
                        ".onthegocontenttest( assesmentname, testinstruction, testendnote, " +
                        " startdate, enddate, scorecorrect, scoreincorrect, " +
                        "  duration,userid,territories, divid, createdon, createdby,timeperquestion," +
                        " applyscoring,showfeedback,allowreview,applyinterval) " +
                        " VALUES(?,?,?,?,?,?,?,CAST(? AS INTERVAL),?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, node.get("name").asText());
                stmt.setString(2, "");
                stmt.setString(3, "");
                stmt.setTimestamp(4, Timestamp.valueOf(node.get("startDate").asText()));
                stmt.setTimestamp(5, Timestamp.valueOf(node.get("endDate").asText()));
                stmt.setArray(6, scoreCrArr);
                stmt.setArray(7, scoreInCrArr);
                stmt.setObject(8, "00:00");
                stmt.setArray(9, userIdArr);
                stmt.setArray(10, terrArr);
                stmt.setInt(11, node.get("divId").asInt());
                stmt.setTimestamp(12, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(13, loggedInUser.id);
                stmt.setArray(14, scoreCrArr);
                stmt.setBoolean(15, false);
                stmt.setBoolean(16, false);
                stmt.setBoolean(17, false);
                stmt.setBoolean(18, false);

                result = stmt.executeUpdate();
                if (result == 0)
                    throw new SQLException("Add On The Go Assesment Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int assesmentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in quesCollectionId
                    assesmentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return assesmentId;

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
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteAssesment(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".onthegocontenttest WHERE id = ?");

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
     * Method is used to update Assesment Details.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateAssesment(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet = null;

            try {
                if (con != null) {
                    Integer[] territories = new Integer[node.withArray("territories").size()];

                    for (int i = 0; i < node.withArray("territories").size(); i++) {
                        territories[i] = node.withArray("territories").get(i).asInt();
                    }

                    Array terrArr = con.createArrayOf("int", territories);

                    Integer[] users = new Integer[node.withArray("territories").size()];
                    stmt = con.prepareStatement(" SELECT userid from " + schemaName + ".userterritorymap " +
                            " WHERE terrid = ? ");
                    for (int i = 0; i < territories.length; i++) {
                        stmt.setInt(1, territories[i]);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            users[i] = resultSet.getInt(1);
                            System.out.println("Id : " + resultSet.getInt(1));
                        }
                    }

                    Array userIdArr = con.createArrayOf("int", users);
                    System.out.println(" User Ids  : " + users.length);

                    stmt = con.prepareStatement(" UPDATE " + schemaName + ".onthegocontenttest " +
                            " SET assesmentname = ? , territories = ? , userid = ?,startdate = ?,enddate = ? " +
                            " WHERE id = ? ");
                    stmt.setString(1, node.get("name").asText());
                    stmt.setArray(2, terrArr);
                    stmt.setArray(3, userIdArr);
                    stmt.setTimestamp(4, Timestamp.valueOf(node.get("startDate").asText()));
                    stmt.setTimestamp(5, Timestamp.valueOf(node.get("endDate").asText()));
                    stmt.setInt(6, node.get("id").asInt());

                    affectedRows = stmt.executeUpdate();
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
            return affectedRows;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to update assesment setting details.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateSettings(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                    Array correctScoreArray = null;
                    Array inCorrectScoreArray = null;
                    Array diffTimeArray = null;

                    stmt = con.prepareStatement(" UPDATE " + schemaName + ".onthegocontenttest " +
                            " SET  testinstruction=?, testendnote=?, testdescription = ?, " +
                            " applyscoring=?, scorecorrect=?, scoreincorrect=?, showfeedback=?, duration=CAST (? as INTERVAL )," +
                            " applytimeperquestion=?, allowreview=?, randomdelivery = ?,timeperquestion=? " +
                            " WHERE id = ? ");

                    if (node.has("Instruction"))
                        stmt.setString(1, node.get("Instruction").asText());
                    else
                        stmt.setString(1, null);

                    if (node.has("EndNote"))
                        stmt.setString(2, node.get("EndNote").asText());
                    else
                        stmt.setString(2, null);

                    if (node.has("Description"))
                        stmt.setString(3, node.get("Description").asText());
                    else
                        stmt.setString(3, null);

                    stmt.setBoolean(4, node.get("Scoring").get("IsApplyScoring").asBoolean());

                    if (node.get("Scoring").get("IsApplyScoring").asBoolean()) {
                        Integer correctScore[] = new Integer[node.get("Scoring").get("CorrectScore").size()];
                        Double inCorrectScore[] = new Double[node.get("Scoring").get("IncorrectScore").size()];

                        for (int i = 0; i < node.get("Scoring").get("CorrectScore").size(); i++) {
                            correctScore[i] = node.get("Scoring").get("CorrectScore").get(i).asInt();
                        }
                        for (int i = 0; i < node.get("Scoring").get("IncorrectScore").size(); i++) {
                            inCorrectScore[i] = node.get("Scoring").get("IncorrectScore").get(i).asDouble();
                        }

                        correctScoreArray = con.createArrayOf("int", correctScore);
                        inCorrectScoreArray = con.createArrayOf("FLOAT8", inCorrectScore);
                    } else {
                        Integer correctScore[] = new Integer[]{};
                        Double inCorrectScore[] = new Double[]{};

                        correctScoreArray = con.createArrayOf("int", correctScore);
                        inCorrectScoreArray = con.createArrayOf("FLOAT8", inCorrectScore);
                    }


                    stmt.setArray(5, correctScoreArray);
                    stmt.setArray(6, inCorrectScoreArray);

                    stmt.setBoolean(7, node.get("showFeedBack").asBoolean());

                    stmt.setBoolean(9, node.get("TimeLimitation").get("IsApplyTimePerQuestion").asBoolean());

                    if (node.get("TimeLimitation").get("IsTimeLimitation").asBoolean()) {

                        if (node.get("TimeLimitation").get("IsApplyTimePerQuestion").asBoolean()) {
                            Integer[] differentTime = new Integer[node.get("TimeLimitation").get("DifferentTime").size()];

                            for (int i = 0; i < node.get("TimeLimitation").get("DifferentTime").size(); i++) {
                                differentTime[i] = node.get("TimeLimitation").get("DifferentTime").get(i).asInt();
                            }
                            diffTimeArray = con.createArrayOf("int", differentTime);
                        } else {
                            Integer[] differentTime = new Integer[]{};
                            diffTimeArray = con.createArrayOf("int", differentTime);
                        }
                    } else {
                        Integer[] differentTime = new Integer[]{};
                        diffTimeArray = con.createArrayOf("int", differentTime);
                    }

                    stmt.setArray(12, diffTimeArray);

                    if (node.get("TimeLimitation").has("FixedTime"))
                        stmt.setObject(8, node.get("TimeLimitation").get("FixedTime").asText());
                    else
                        stmt.setObject(8, "00:00");


                    stmt.setBoolean(10, node.get("AllowReview").asBoolean());

                    stmt.setBoolean(11, node.get("randomDelivery").asBoolean());

                    stmt.setInt(13, node.get("id").asInt());

                    affectedRows = stmt.executeUpdate();

                    AssesmentCollection.arrangeAssesmentQuestions(node.get("id").asInt(), loggedInUser);
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
            return affectedRows;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to get All setting details of Assesment.
     *
     * @param testId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getSettingDetails(int testId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int affectedRow = 0;
            List<Assesment> assesmentSettingList = new ArrayList<>();
            Assesment assesmentSetting;
            try {
                stmt = con.prepareStatement("SELECT testinstruction, testendnote,testdescription, applyscoring, " +
                        " scorecorrect, scoreincorrect, showfeedback, duration, applytimeperquestion," +
                        " allowreview,randomdelivery,applyinterval,timeperquestion " +
                        " FROM " + schemaName + ".onthegocontenttest " +
                        " WHERE  id = ? ");
                stmt.setInt(1, testId);
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    assesmentSetting = new Assesment();
                    assesmentSetting.Instrction = resultSet.getString(1);
                    assesmentSetting.EndNote = resultSet.getString(2);
                    assesmentSetting.Description = resultSet.getString(3);

                    assesmentSetting.Scoring = new HashMap();
                    assesmentSetting.Scoring.put("IsApplyScoring", resultSet.getBoolean(4));

                    HashMap CorrectScore = new HashMap();
                    Integer[] corrArray = (Integer[]) resultSet.getArray(5).getArray();
                    if (corrArray.length > 0 && corrArray.length == 3) {
                        CorrectScore.put("Low", corrArray[0]);
                        CorrectScore.put("Medium", corrArray[1]);
                        CorrectScore.put("High", corrArray[2]);
                    }

                    assesmentSetting.Scoring.put("CorrectScore", CorrectScore);

                    HashMap IncorrectScore = new HashMap();
                    Double[] inCorrArray = (Double[]) resultSet.getArray(6).getArray();
                    if (inCorrArray.length > 0 && inCorrArray.length == 3) {
                        IncorrectScore.put("Low", inCorrArray[0]);
                        IncorrectScore.put("Medium", inCorrArray[1]);
                        IncorrectScore.put("High", inCorrArray[2]);
                    }

                    assesmentSetting.Scoring.put("IncorrectScore", IncorrectScore);

                    assesmentSetting.showFeedBack = resultSet.getBoolean(7);

                    assesmentSetting.TimeLimitation = new HashMap();
                    assesmentSetting.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(9));
                    assesmentSetting.TimeLimitation.put("FixedTime", resultSet.getString(8));

                    HashMap DifferentTime = new HashMap();
                    Integer[] diffArr = (Integer[]) resultSet.getArray(13).getArray();
                    if (diffArr.length > 0 && diffArr.length == 3) {
                        DifferentTime.put("Low", diffArr[0]);
                        DifferentTime.put("Medium", diffArr[1]);
                        DifferentTime.put("High", diffArr[2]);
                    }

                    assesmentSetting.TimeLimitation.put("DifferentTime", DifferentTime);

                    assesmentSetting.AllowReview = resultSet.getBoolean(10);
                    assesmentSetting.randomDelivery = resultSet.getBoolean(11);

                    assesmentSettingList.add(assesmentSetting);
                }

            } finally {
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return assesmentSettingList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getRunningAssesment(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            ResultSet countSet = null;
            List<Assesment> assesmentList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                   /* stmt = con.prepareStatement("SELECT o.id,assesmentname, o.startdate, o.enddate," +
                            "  o.userid, o.divid, o.createdon, o.createdby " +
                            " FROM " + schemaName + ".onthegocontenttest o " +
                            " WHERE ? = ANY(userid :: int[]) " +
                            " AND (now() BETWEEN o.startdate AND o.enddate OR now() = o.startdate OR now() = o.enddate)");*/

                    stmt = con.prepareStatement("SELECT o.id,assesmentname, o.startdate, o.enddate," +
                            "  o.userid, o.divid, o.createdon, o.createdby " +
                            " FROM " + schemaName + ".onthegocontenttest o " +
                            " WHERE ? = ANY(userid :: int[]) " +
                            " AND (now() AT TIME ZONE 'Asia/Calcutta' BETWEEN o.startdate AND o.enddate " +
                            " OR now() AT TIME ZONE 'Asia/Calcutta' = o.startdate " +
                            " OR now() AT TIME ZONE 'Asia/Calcutta' = o.enddate)");

                    stmt.setInt(1, loggedInUser.id);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        stmt = con.prepareStatement(" SELECT Count(*) As Count FROM " + schemaName + ".onthegoassessmentactualresult" +
                                " WHERE testid = ? AND userid = ? ");
                        stmt.setInt(1, resultSet.getInt(1));
                        stmt.setInt(2, loggedInUser.id);
                        countSet = stmt.executeQuery();
                        while (countSet.next()) {
                            Assesment assesment = new Assesment();

                            if (countSet.getInt("Count") == 0) {

                                assesment.id = resultSet.getInt(1);
                                assesment.name = resultSet.getString(2);
                                assesment.startDate = resultSet.getTimestamp(3);
                                assesment.endDate = resultSet.getTimestamp(4);

                                assesmentList.add(assesment);
                            }

                        }
                    }
                } else
                    throw new Exception("DB connection is null");
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return assesmentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getExpiredAssesment(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            ResultSet countSet = null;
            List<Assesment> assesmentList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT o.id,assesmentname,o.startdate,o.enddate " +
                            " FROM " + schemaName + ".onthegocontenttest o " +
                            " WHERE ? = ANY(userid :: int[]) AND now() AT TIME ZONE 'Asia/Calcutta' > enddate ");

                    stmt.setInt(1, loggedInUser.id);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        Assesment assesment = new Assesment();
                        assesment.id = resultSet.getInt(1);
                        assesment.name = resultSet.getString(2);
                        assesment.startDate = resultSet.getTimestamp(3);
                        assesment.endDate = resultSet.getTimestamp(4);

                        stmt = con.prepareStatement(" SELECT Count(*) As Count FROM " + schemaName + ".onthegoassessmentactualresult" +
                                " WHERE testid = ? AND userid = ? ");
                        stmt.setInt(1, resultSet.getInt(1));
                        stmt.setInt(2, loggedInUser.id);
                        countSet = stmt.executeQuery();
                        while (countSet.next()) {
                            if (countSet.getInt("Count") > 0) {
                                assesment.isExpired = false;
                            } else {
                                assesment.isExpired = true;
                            }
                        }
                        assesmentList.add(assesment);
                    }
                } else
                    throw new Exception("DB connection is null");
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (resultSet != null)
                    if (!resultSet.isClosed())
                        resultSet.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return assesmentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getPastExamScore(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            ResultSet countSet = null;
            List<Assesment> assesmentList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;

            try {
                if (con != null) {
                    stmt = con.prepareStatement(" SELECT sum(score),r.testid,assesmentname,startdate,enddate " +
                            " FROM (select * from " + schemaName + ".onthegoassessmentactualresult r " +
                            " WHERE isattemp = true AND r.userid = ?)r " +
                            " left join " + schemaName + ".onthegocontenttest o on o.id = r.testid " +
                            " GROUP BY r.testid,assesmentname,startdate,enddate");
                    stmt.setInt(1, loggedInUser.id);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        Assesment assesment = new Assesment();
                        assesment.score = resultSet.getInt(1);
                        assesment.name = resultSet.getString(3);
                        assesment.startDate = resultSet.getTimestamp(4);
                        assesment.endDate = resultSet.getTimestamp(5);
                        assesmentList.add(assesment);
                    }

                  /*  stmt = con.prepareStatement("SELECT id,assesmentname,startdate,enddate "+
                            " FROM " + schemaName + ".onthegocontenttest " +
                            " WHERE ? = ANY(userid :: int[]) AND now() > enddate ");
                    stmt.setInt(1,loggedInUser.id);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        Assesment assesment = new Assesment();
                        assesment.id = resultSet.getInt(1);
                        assesment.name = resultSet.getString(2);
                        assesment.startDate = resultSet.getTimestamp(3);
                        assesment.endDate = resultSet.getTimestamp(4);

                        stmt = con.prepareStatement(" SELECT userid,sum(score) " +
                                " FROM "+schemaName+".onthegoassessmentactualresult  " +
                                " WHERE isattemp = true AND testid = ? AND userid = ? GROUP BY(userid,testid)");
                        stmt.setInt(1,resultSet.getInt(1));
                        stmt.setInt(2,loggedInUser.id);
                        countSet = stmt.executeQuery();
                        while (countSet.next())
                        {
                            assesment.score = resultSet.getInt(1);
                        }
                        assesmentList.add(assesment);
                    }*/
                } else
                    throw new Exception("DB connection is null");
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
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
            return assesmentList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param testId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Assesment> getAssesmentQuestionSet(int testId, LoggedInUser loggedInUser) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        List<Assesment> collectionList = new ArrayList<>();
        ResultSet resultSet = null;
        String schemaname = loggedInUser.schemaName;
        boolean isRandom = false;
        Integer[] quesArray = new Integer[0];

        try {

            stmt = con.prepareStatement(" SELECT questionids,israndom " +
                    " FROM " + schemaname + ".onthegoassesmentactual WHERE testid = ? ");
            stmt.setInt(1, testId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                quesArray = (Integer[]) resultSet.getArray(1).getArray();
            }

            stmt = con.prepareStatement(" SELECT assesmentname, testinstruction, testendnote, testdescription," +
                    " allowreview, timeperquestion,applytimeperquestion, duration,randomdelivery " +
                    " FROM " + schemaname + ".onthegocontenttest " +
                    " WHERE id  = ? ");

            stmt.setInt(1, testId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Assesment assesment = new Assesment();

                assesment.name = resultSet.getString(1);
                assesment.Instrction = resultSet.getString(2);
                assesment.EndNote = resultSet.getString(3);
                assesment.Description = resultSet.getString(4);
                assesment.AllowReview = resultSet.getBoolean(5);

                assesment.TimeLimitation = new HashMap();
                assesment.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(7));
                assesment.TimeLimitation.put("FixedTime", resultSet.getString(8));

                HashMap DifferentTime = new HashMap();
                if (resultSet.getArray(6) != null) {
                    Integer[] diffArr = (Integer[]) resultSet.getArray(6).getArray();
                    if (diffArr.length > 0 && diffArr.length == 3) {
                        DifferentTime.put("Low", diffArr[0]);
                        DifferentTime.put("Medium", diffArr[1]);
                        DifferentTime.put("High", diffArr[2]);
                    }
                }

                assesment.TimeLimitation.put("DifferentTime", DifferentTime);

                assesment.randomDelivery = resultSet.getBoolean(9);
                isRandom = resultSet.getBoolean(9);

///                   questionCollectionList.add(collection);
                assesment.questions = new ArrayList<>();

                System.out.println("Length : " + quesArray.length);
                for (int i = 0; i < quesArray.length; i++) {
                    System.out.println("Array " + i + " : " + quesArray[i]);
                    assesment.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(quesArray[i], loggedInUser));
                }
                collectionList.add(assesment);
            }
            if (isRandom) {
                Collections.shuffle(collectionList);
            }
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
        return collectionList;
    }
}
