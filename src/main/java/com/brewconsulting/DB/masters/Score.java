package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by lcom62_one on 1/6/2017.
 */
public class Score {

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("questionId")
    public int questionId;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("questionJson")
    public String questionJson;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("answerJson")
    public String answerJson;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("score")
    public double score;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("collectionName")
    public String collectionName;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("userId")
    public int userId;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("username")
    public String username;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("fullname")
    public String fullname;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("correctAnswer")
    public int correctAnswer;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("inCorrectAnswer")
    public int inCorrectAnswer;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("notAttempt")
    public int notAttempt;

    @JsonView({UserViews.sessionView.class})
    @JsonProperty("id")
    public int id;

    @JsonView({UserViews.sessionView.class})
    @JsonProperty("isSessionStart")
    public boolean isSessionStart;

    @JsonProperty("sessionStartTime")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:MM:ss")
    public String sessionStartTime;

    @JsonView({UserViews.sessionView.class})
    @JsonProperty("isSessionEnd")
    public boolean isSessionEnd;

    @JsonProperty("sessionEndTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:MM:ss")
    public String sessionEndTime;

    @JsonView({UserViews.sessionView.class})
    @JsonProperty("deliveryMode")
    public String deliveryMode;

    /***
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addUsersAllAnswer(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet;
            String schemaname = loggedInUser.schemaName;
            double score = 0;
            Integer[] correctScore = new Integer[0];
            Double[] inCorrectScore = new Double[0];
            boolean isApplyScoring = false;
            int count = 0;
            boolean isReviewQuestion = false;

            try {
                stmt = con.prepareStatement(" SELECT applyscoring,scorecorrect,scoreincorrect FROM " + schemaname
                        + ".cyclemeetingsessioncontenttest WHERE agendaid = ? ");
                stmt.setInt(1, node.get("agendaId").asInt());
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    isApplyScoring = resultSet.getBoolean(1);
                    correctScore = (Integer[]) resultSet.getArray(2).getArray();
                    inCorrectScore = (Double[]) resultSet.getArray(3).getArray();
                }

                for (int i = 0; i < node.withArray("answerSet").size(); i++) {
                    stmt = con.prepareStatement("SELECT id,complexitylevel,questionjson,answerjson,isreview " +
                            " FROM " + schemaname + ".question WHERE id = ? ");
                    System.out.println("Question ID : " + node.withArray("answerSet").get(i).get("questionId").asInt());
                    stmt.setInt(1, node.withArray("answerSet").get(i).get("questionId").asInt());
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        stmt = con.prepareStatement(" INSERT INTO " + schemaname
                                + ".cyclemeetingassessmentresult(questionid, userid, answerjson, questionjson, useranswerjson, score,agendaid,isattemp,isreview)" +
                                " VALUES(?, ?, ?, ?, ?, ?,?,?,?) ");
                        stmt.setInt(1, resultSet.getInt(1));
                        stmt.setInt(2, node.get("userId").asInt());
                        System.out.println("Right Answer : " + resultSet.getString(4));
                        stmt.setString(3, resultSet.getString(4));
                        isReviewQuestion = resultSet.getBoolean(5);
                        stmt.setString(4, resultSet.getString(3));
                        System.out.println("Answer JSON : " + String.valueOf(node.withArray("answerSet").get(i).get("answerJson")));
                        stmt.setString(5, String.valueOf(node.withArray("answerSet").get(i).get("answerJson")));

                        if (isReviewQuestion) {
                            stmt.setDouble(6, 0);
                            stmt.setBoolean(9, true);

                            if (String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).isEmpty() ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals(null) ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals("")) {
                                stmt.setBoolean(8, false);
                            } else {
                                stmt.setBoolean(8, true);
                            }
                        } else {
                            stmt.setBoolean(9, false);
                            if (String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).isEmpty() ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals(null) ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals("")) {
                                stmt.setDouble(6, 0);
                                stmt.setBoolean(8, false);
                            } else {
                                if (isApplyScoring) {
                                    if (resultSet.getString(4).equals(String.valueOf(node.withArray("answerSet").get(i).get("answerJson")))) {
                                        if (correctScore.length > 0) {
                                            if (resultSet.getString(2).equals("LOW")) {
                                                score = correctScore[0];
                                            }
                                            if (resultSet.getString(2).equals("MEDIUM")) {
                                                score = correctScore[1];
                                            }
                                            if (resultSet.getString(2).equals("HIGH")) {
                                                score = correctScore[2];
                                            }
                                        }
                                        stmt.setDouble(6, score);
                                        stmt.setBoolean(8, true);
                                    } else {
                                        if (inCorrectScore.length > 0) {
                                            if (resultSet.getString(2).equals("LOW")) {
                                                score = inCorrectScore[0];
                                            }
                                            if (resultSet.getString(2).equals("MEDIUM")) {
                                                score = inCorrectScore[1];
                                            }
                                            if (resultSet.getString(2).equals("HIGH")) {
                                                score = inCorrectScore[2];
                                            }
                                        }
                                        stmt.setDouble(6, -score);
                                        stmt.setBoolean(8, true);
                                    }
                                }
                            }
                        }
                        stmt.setInt(7, node.get("agendaId").asInt());
                        result = stmt.executeUpdate();
                        count++;
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
            return count;
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
    public static int addUsersAnswer(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet;
            ResultSet updateResultSet;
            String schemaname = loggedInUser.schemaName;
            boolean isApplyScoring = false;
            Integer[] correctScore = new Integer[0];
            Double[] inCorrectScore = new Double[0];
            double score = 0;
            int resultId = 0;
            String actualAnswer = "";
            boolean isReviewQuestion = false;

            try {
                if (con != null) {
                    stmt = con.prepareStatement(" SELECT applyscoring,scorecorrect,scoreincorrect FROM " + schemaname
                            + ".cyclemeetingsessioncontenttest WHERE agendaid = ? ");
                    stmt.setInt(1, node.get("agendaId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        isApplyScoring = resultSet.getBoolean(1);
                        correctScore = (Integer[]) resultSet.getArray(2).getArray();
                        inCorrectScore = (Double[]) resultSet.getArray(3).getArray();
                    }

                    stmt = con.prepareStatement("SELECT id,complexitylevel,questionjson,answerjson,isreview " +
                            " FROM " + schemaname + ".question WHERE id = ? ");
                    System.out.println("Question ID : " + node.get("questionId").asInt());
                    stmt.setInt(1, node.get("questionId").asInt());
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        actualAnswer = resultSet.getString(4);
                        isReviewQuestion = resultSet.getBoolean(5);

                        stmt = con.prepareStatement(" SELECT count(*) AS Count FROM " + schemaname + ".cyclemeetingassessmentresult" +
                                " WHERE agendaid = ? AND userid = ? AND questionid = ? ");
                        stmt.setInt(1, node.get("agendaId").asInt());
                        stmt.setInt(2, node.get("userId").asInt());
                        stmt.setInt(3, node.get("questionId").asInt());
                        updateResultSet = stmt.executeQuery();
                        while (updateResultSet.next()) {
                            if (updateResultSet.getInt("Count") > 0) {
                                stmt = con.prepareStatement(" UPDATE " + schemaname + ".cyclemeetingassessmentresult " +
                                        " SET answerjson = ?, questionjson = ?, useranswerjson = ?, score = ?,isattemp = ? ,isreview = ?" +
                                        " WHERE agendaid = ? AND userid = ? AND questionid = ?");
                                stmt.setString(1, actualAnswer);
                                stmt.setString(2, resultSet.getString(3));
                                stmt.setString(3, String.valueOf(node.get("answerJson")));

                                if (isReviewQuestion) {
                                    stmt.setDouble(4, 0);
                                    stmt.setBoolean(6, true);

                                    if (String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")) {
                                        stmt.setBoolean(5, false);
                                    } else {
                                        stmt.setBoolean(5, true);
                                    }
                                } else {
                                    stmt.setBoolean(6, false);
                                    if (String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")) {
                                        stmt.setDouble(4, 0);
                                        stmt.setBoolean(5, false);
                                    } else {
                                        if (isApplyScoring) {
                                            if (actualAnswer.equals(String.valueOf(node.get("answerJson")))) {
                                                if (correctScore.length > 0) {
                                                    if (resultSet.getString(2).equals("LOW")) {
                                                        score = correctScore[0];
                                                    }
                                                    if (resultSet.getString(2).equals("MEDIUM")) {
                                                        score = correctScore[1];
                                                    }
                                                    if (resultSet.getString(2).equals("HIGH")) {
                                                        score = correctScore[2];
                                                    }
                                                }
                                                stmt.setDouble(4, score);
                                                stmt.setBoolean(5, true);
                                            } else {
                                                if (inCorrectScore.length > 0) {
                                                    if (resultSet.getString(2).equals("LOW")) {
                                                        score = inCorrectScore[0];
                                                    }
                                                    if (resultSet.getString(2).equals("MEDIUM")) {
                                                        score = inCorrectScore[1];
                                                    }
                                                    if (resultSet.getString(2).equals("HIGH")) {
                                                        score = inCorrectScore[2];
                                                    }
                                                }
                                                stmt.setDouble(4, -score);
                                                stmt.setBoolean(5, true);
                                            }
                                        }
                                    }
                                }

                                stmt.setInt(7, node.get("agendaId").asInt());
                                stmt.setInt(8, node.get("userId").asInt());
                                stmt.setInt(9, node.get("questionId").asInt());
                            } else {
                                stmt = con.prepareStatement(" INSERT INTO " + schemaname
                                        + ".cyclemeetingassessmentresult(questionid, userid, answerjson, questionjson, useranswerjson, score,agendaid,isattemp,isreview)" +
                                        " VALUES(?, ?, ?, ?, ?, ?,?,?,?) ", Statement.RETURN_GENERATED_KEYS);
                                stmt.setInt(1, resultSet.getInt(1));
                                stmt.setInt(2, node.get("userId").asInt());
                                System.out.println("Right Answer : " + resultSet.getString(4));
                                stmt.setString(3, resultSet.getString(4));
                                stmt.setString(4, resultSet.getString(3));
                                System.out.println("Answer JSON : " + String.valueOf(node.get("answerJson")));
                                stmt.setString(5, String.valueOf(node.get("answerJson")));

                                if ((actualAnswer.isEmpty() || actualAnswer.equals(null) || actualAnswer.equals(""))) {
                                    stmt.setDouble(6, 0);
                                    stmt.setBoolean(9, true);

                                    if (String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")) {
                                        stmt.setBoolean(8, false);
                                    } else {
                                        stmt.setBoolean(8, true);
                                    }
                                } else {
                                    stmt.setBoolean(9, false);
                                    if (String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")) {
                                        stmt.setDouble(6, 0);
                                        stmt.setBoolean(8, false);
                                    } else {
                                        if (isApplyScoring) {
                                            if (resultSet.getString(4).equals(String.valueOf(node.get("answerJson")))) {
                                                if (correctScore.length > 0) {
                                                    if (resultSet.getString(2).equals("LOW")) {
                                                        score = correctScore[0];
                                                    }
                                                    if (resultSet.getString(2).equals("MEDIUM")) {
                                                        score = correctScore[1];
                                                    }
                                                    if (resultSet.getString(2).equals("HIGH")) {
                                                        score = correctScore[2];
                                                    }
                                                }
                                                stmt.setDouble(6, score);
                                                stmt.setBoolean(8, true);
                                            } else {
                                                if (inCorrectScore.length > 0) {
                                                    if (resultSet.getString(2).equals("LOW")) {
                                                        score = inCorrectScore[0];
                                                    }
                                                    if (resultSet.getString(2).equals("MEDIUM")) {
                                                        score = inCorrectScore[1];
                                                    }
                                                    if (resultSet.getString(2).equals("HIGH")) {
                                                        score = inCorrectScore[2];
                                                    }
                                                }
                                                stmt.setDouble(6, -score);
                                                stmt.setBoolean(8, true);
                                            }
                                        }
                                        stmt.setDouble(6, score);
                                        stmt.setBoolean(8, true);
                                    }
                                }

                                stmt.setInt(7, node.get("agendaId").asInt());
                                result = stmt.executeUpdate();

                                if (result == 0)
                                    throw new SQLException("Add Question Collection Failed.");

                                ResultSet generatedKeys = stmt.getGeneratedKeys();
                                if (generatedKeys.next())
                                    // It gives last inserted Id in quesCollectionId
                                    resultId = generatedKeys.getInt(1);
                                else
                                    throw new SQLException("No ID obtained");
                            }
                        }
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
            return resultId;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method is used to Get User's Total Score.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getUsersWiseResult(int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();

            try {
                if (con != null) {
                    stmt = con.prepareStatement(
                            " SELECT count(case when score>0 and isattemp = true and isreview = false then 1 else null end) as CorrectAnswer," +
                                    " count(case when score=0 and isattemp = true and isreview = false and answerjson != useranswerjson then 1 else null end) as IncorrectAnswer," +
                                    "count(case when isattemp = false then 1 else null end) as notAttempt," +
                                    " sum(score) as TotalScore, userid,username,firstname,lastname" +
                                    " FROM " + schemaname + ".cyclemeetingassessmentresult " +
                                    " left join master.users u on u.id = userid " +
                                    " WHERE agendaid = ? GROUP BY(userid,u.username,u.firstname,u.lastname) ORDER BY totalscore DESC ");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        Score score = new Score();
                        score.correctAnswer = resultSet.getInt(1);
                        score.inCorrectAnswer = resultSet.getInt(2);
                        score.notAttempt = resultSet.getInt(3);
                        score.score = resultSet.getDouble(4);
                        score.userId = resultSet.getInt(5);
                        score.username = resultSet.getString(6);
                        score.fullname = resultSet.getString(7) + " " + resultSet.getString(8);
                        scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method is used to Get User's Total Score.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getQuestionWiseResult(int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();

            try {
                if (con != null) {
                    stmt = con.prepareStatement(
                            " SELECT count(case when score>0 and isattemp = true and isreview = false then 1 else null end) as CorrectAnswer," +
                                    " count(case when score=0 and isattemp = true and isreview = false and answerjson != useranswerjson then 1 else null end) as IncorrectAnswer," +
                                    " count(case when isattemp = false then 1 else null end) as notAttempt," +
                                    " questionid,questionjson,answerjson" +
                                    " FROM " + schemaname + ".cyclemeetingassessmentresult " +
                                    " WHERE agendaid = ? GROUP BY(questionid)");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        Score score = new Score();
                        score.correctAnswer = resultSet.getInt(1);
                        score.inCorrectAnswer = resultSet.getInt(2);
                        score.notAttempt = resultSet.getInt(3);
                        score.questionId = resultSet.getInt(4);
                        score.questionJson = resultSet.getString(5);
                        score.answerJson = resultSet.getString(6);
                        scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to get Specific Question wise user's Score.
     *
     * @param userId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getUsersQuestionWiseScore(int userId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Score> scoreList = new ArrayList<>();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            String schemaname = loggedInUser.schemaName;

            try {
                if (con != null) {
                    stmt = con.prepareStatement(" SELECT questionid,r.questionjson,score,userid,username " +
                            " FROM " + schemaname + ".cyclemeetingassessmentresult r" +
                            " left join " + schemaname + ".question q on q.id = questionid" +
                            " left join master.users u on u.id = userid" +
                            " WHERE userid = ? ");
                    stmt.setInt(1, userId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        Score score = new Score();
                        score.questionId = resultSet.getInt(1);
                        score.questionJson = resultSet.getString(2);
                        score.score = resultSet.getDouble(3);
                        score.userId = resultSet.getInt(4);
                        score.username = resultSet.getString(5);
                        scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getUserWiseParticularScore(int userId, int agendaId, String mode, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            String condition = "";
            try {
                if (mode == "correct") {
                    condition = "AND score > 0 AND isattemp = true AND isReview=false";
                } else if (mode == "incorrect") {
                    condition = "AND isattemp = true AND answerjson != useranswerjson AND isReview=false";
                } else if (mode == "notattempt") {
                    condition = "AND isattemp = false";
                }

                stmt = con.prepareStatement(" SELECT questionid,r.questionjson,r.answerjson,score " +
                        " FROM " + schemaname + ".cyclemeetingassessmentresult r" +
                        " WHERE userid = ? AND agendaid = ? " + condition);
                stmt.setInt(1, userId);
                stmt.setInt(2, agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    Score score = new Score();
                    score.questionId = resultSet.getInt(1);
                    score.questionJson = resultSet.getString(2);
                    score.answerJson = resultSet.getString(3);
                    score.score = resultSet.getDouble(4);
                    scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getQuestionWiseUserStatus(int questionId, int agendaId, String mode, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            String condition = "";
            try {
                if (mode.equals("correct")) {
                    condition = "AND score > 0 AND isattemp = true AND isReview=false";
                } else if (mode.equals("incorrect")) {
                    condition = "AND isattemp = true AND answerjson != useranswerjson AND isReview=false";
                } else if (mode.equals("notattempt")) {
                    condition = "AND isattemp = false";
                }

                stmt = con.prepareStatement(" SELECT r.userid,score,u.username,u.firstname,u.lastname " +
                        " FROM " + schemaname + ".cyclemeetingassessmentresult r " +
                        " left join master.users u on u.id = r.userid " +
                        " WHERE questionid = ? AND agendaid = ? " + condition);
                stmt.setInt(1, questionId);
                stmt.setInt(2, agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    Score score = new Score();
                    score.questionId = resultSet.getInt(1);
                    score.score = resultSet.getDouble(2);
                    score.username = resultSet.getString(3);
                    score.fullname = resultSet.getString(4) + " " + resultSet.getString(5);
                    scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getUserTotalScore(int userId, int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;

            try {
                stmt = con.prepareStatement(" SELECT r.userid,r.score,u.username,u.firstname,u.lastname, r.questionjson" +
                        " FROM " + schemaname + ".cyclemeetingassessmentresult r " +
                        " left join master.users u on u.id = r.userid " +
                        " WHERE isattemp = true AND userid = ? AND agendaid = ? ");
                stmt.setInt(1, userId);
                stmt.setInt(2, agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    Score score = new Score();
                    score.userId = resultSet.getInt(1);
                    score.score = resultSet.getDouble(2);
                    score.username = resultSet.getString(3);
                    score.fullname = resultSet.getString(4) + " " + resultSet.getString(5);
                    score.questionJson = resultSet.getString(6);
                    scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getUsersAllExamsScore(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<Score> scoreList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;

            try {
                stmt = con.prepareStatement(" SELECT r.userid,r.score,cc.collectionname,a.sessionname" +
                        " FROM " + schemaname + ".cyclemeetingassessmentresult r " +
                        " left join master.users u on u.id = r.userid " +
                        " left join " + schemaname + ".cyclemeetingsessioncontenttestquestioncollection cc on cc.agendaid = r.agendaid " +
                        " left join " + schemaname + ".cyclemeetingagenda a on a.id = r.agendaid " +
                        " WHERE isattemp = true AND userid = ? GROUP BY r.agendaid,userid,r.score,cc.collectionname,a.sessionname ");
                stmt.setInt(1, loggedInUser.id);
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    Score score = new Score();
                    score.userId = resultSet.getInt(1);
                    score.score = resultSet.getDouble(2);
                    score.collectionName = resultSet.getString(4);
                    scoreList.add(score);
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
            return scoreList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method is used to get Session Details that session is start or end.
     *
     * @param meetingId
     * @param sessionId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Score> getSessionDetails(int meetingId, int sessionId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 15).equals("Read") ||
                Permissions.isAuthorised(userRole, 15).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Score> sessionList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            String deliveryMode;

            try {
                stmt = con.prepareStatement(" SELECT id,sessionstarttime,sessionendtime " +
                        " FROM " + schemaname + ".cyclemeetingactualtimes WHERE cyclemeetingid = ? AND sessionid = ? ");
                stmt.setInt(1, meetingId);
                stmt.setInt(2, sessionId);
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    Score score = new Score();
                    score.id = resultSet.getInt(1);

                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Or whatever IST is supposed to be

                    score.sessionStartTime = formatter.format(resultSet.getTimestamp(2));
                    System.out.println("Result : " + resultSet.getTimestamp(2) + " : Start Time : " + score.sessionStartTime);

//                            resultSet.getTimestamp(2);
                    if (!resultSet.getTimestamp(2).equals(null))
                        score.isSessionStart = true;
                    else
                        score.isSessionStart = false;

                    score.sessionEndTime = formatter.format(resultSet.getTimestamp(3));
                    System.out.println("Result : " + resultSet.getTimestamp(3) + " : End Time : " + score.sessionEndTime);
//                            resultSet.getTimestamp(3);
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
                    String date = sd.format(resultSet.getTimestamp(3));

                    if (date.equals("1970-01-01"))
                        score.isSessionEnd = false;
                    else
                        score.isSessionEnd = true;

                    stmt = con.prepareStatement(" SELECT deliverymode FROM " + schemaname + ".cyclemeetingsessioncontenttest" +
                            " WHERE agendaid = ? ");
                    stmt.setInt(1, sessionId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        score.deliveryMode = resultSet.getString(1);
                    }
                    sessionList.add(score);
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
            return sessionList;
        } else {
            throw new NotAuthorizedException("");
        }
    }
}
