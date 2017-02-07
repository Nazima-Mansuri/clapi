package com.brewconsulting.DB.masters;

import com.amazonaws.services.dynamodbv2.xspec.NULL;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.naming.NamingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lcom53 on 15/12/16.
 */
public class QuestionCollection {

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("id")
    public int id;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("agendaId")
    public int agendaId;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("contentType")
    public String contentType;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("contentSeq")
    public int contentSeq;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("createBy")
    public int createBy;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdOn")
    public java.util.Date createdOn;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("updateOn")
    public java.util.Date updateOn;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("updateBy")
    public int updateBy;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("title")
    public String title;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("description")
    public String description;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("questionsId")
    public Integer[] questionsId;

    @JsonView({UserViews.collectionView.class, UserViews.settingView.class})
    @JsonProperty("randomdelivery")
    public boolean randomdelivery;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("collectionseq")
    public int collectionseq;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("disregardcomplexitylevel")
    public boolean disregardcomplexitylevel;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("deliverallquestions")
    public boolean deliverallquestions;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("questionbreakup")
    public Integer[] questionbreakup;

    @JsonView({UserViews.collectionView.class})
    @JsonProperty("collection")
    public String collection;

    @JsonView({UserViews.quesSetView.class})
    @JsonProperty("agendaName")
    public String agendaName;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("Instruction")
    public String Instrction;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("EndNote")
    public String EndNote;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("Description")
    public String Description;

    @JsonView({UserViews.settingView.class})
    @JsonProperty("showFeedBack")
    public boolean showFeedBack;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("AllowReview")
    public boolean AllowReview;

    @JsonView({UserViews.settingView.class})
    @JsonProperty("Scoring")
    public HashMap Scoring;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("TimeLimitation")
    public HashMap TimeLimitation;

    @JsonView({UserViews.settingView.class, UserViews.quesSetView.class})
    @JsonProperty("deliveryMode")
    public String deliveryMode;

    @JsonView({UserViews.collectionView.class, UserViews.quesSetView.class})
    @JsonProperty("questions")
    public List<Question> questions;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("IsApplyScoring")
    public boolean IsApplyScoring;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("scorecorrect")
    public Integer[] scorecorrect;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("scoreIncorrect")
    public Double[] scoreIncorrect;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("duration")
    public String duration;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("applyInterval")
    public boolean applyInterval;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("differentTime")
    public Integer[] differentTime;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("applytimeperquestion")
    public boolean applytimeperquestion;

/*    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("isAttempted")
    public boolean isAttempted;

    @JsonView({UserViews.quesAgendaView.class})
    @JsonProperty("isReview")
    public boolean isReview;*/

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
    @JsonProperty("userAnswerJson")
    public String userAnswerJson;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("isReview")
    public boolean isReview;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("score")
    public double score;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("imageURL")
    public String imageURL;

    @JsonView({UserViews.scoreView.class})
    @JsonProperty("fileType")
    public String fileType;

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


    // make visible to package only
    public QuestionCollection() {
    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    public enum DeliveryMode {
        WEB, APP;
    }

    public static final int Question = 15;


    /***
     * Method is used to get all question collections.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addQuestionCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int result;

            try {
                con.setAutoCommit(false);


                // TODO: set up the reminder int array
                Integer[] questionArr = new Integer[node.withArray("questions").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("questions").size(); i++) {
                    questionArr[i] = node.withArray("questions").get(i).asInt();
                }

                Array question = con.createArrayOf("int", questionArr);

                Integer[] quesbrkArr = new Integer[node.withArray("questionbreakup").size()];

                // Convert JsonArray into String Array
                for (int i = 0; i < node.withArray("questionbreakup").size(); i++) {
                    quesbrkArr[i] = node.withArray("questionbreakup").get(i).asInt();
                }

                Array quesBreak = con.createArrayOf("int", quesbrkArr);

                ContentType typeContent = ContentType.valueOf(node.get("contentType").asText());

                if (node.get("isGroup").asBoolean()) {
                    System.out.println("In GROUP..");
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection"
                                            + " (agendaid,contenttype,contentseq,createdon,createdby,updateon,updatedby,"
                                            + " questions,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                            + " questionbreakup,collectionname) values (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);
                } else {
                    System.out.println("In CYCLE MEETING..");
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection"
                                            + " (agendaid,contenttype,contentseq,createdon,createdby,updateon,updatedby,"
                                            + " questions,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                            + " questionbreakup,collectionname) values (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);
                }

                stmt.setInt(1, node.get("agendaId").asInt());
                stmt.setString(2, typeContent.name());
                stmt.setInt(3, 1);
                stmt.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(5, loggedInUser.id);
                stmt.setTimestamp(6, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(7, loggedInUser.id);
                stmt.setArray(8, question);
                stmt.setInt(9, 1);
                stmt.setBoolean(10, node.get("disregardcomplexitylevel").asBoolean());
                stmt.setBoolean(11, node.get("deliverallquestions").asBoolean());
                stmt.setArray(12, quesBreak);
                stmt.setString(13, node.get("collectionname").asText());
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Question Collection Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int quesCollectionId;
                if (generatedKeys.next())
                    // It gives last inserted Id in quesCollectionId
                    quesCollectionId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();

                arrangeQuestions(node.get("agendaId").asInt(), loggedInUser, node.get("isGroup").asBoolean(), node.get("meetingId").asInt());

                return quesCollectionId;

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
     * This method is used to get All Group or Cycle meeting Queston Collections.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getAllQuestionCollection(int agendaId, boolean isGroup, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<QuestionCollection> collectionsList = new ArrayList<QuestionCollection>();
            QuestionCollection questionCollection;
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet questionResult = null;

            try {
                if (con != null) {

                    if (isGroup) {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby, questions, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup," +
                                " collectionname, title, description " +
                                " FROM " + schemaName + ".groupsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                    } else {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby,questions, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup," +
                                " collectionname, title, description " +
                                " FROM " + schemaName + ".cyclemeetingsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                    }

                    stmt.setInt(1, agendaId);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        questionCollection = new QuestionCollection();

                        questionCollection.id = result.getInt(1);
                        questionCollection.agendaId = result.getInt(2);
                        questionCollection.contentType = result.getString(3);
                        questionCollection.contentSeq = result.getInt(4);
                        questionCollection.createdOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        questionCollection.createBy = result.getInt(6);
                        questionCollection.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(7).getTime())));
                        questionCollection.updateBy = result.getInt(8);
                        questionCollection.questionsId = (Integer[]) result.getArray(9).getArray();
                        questionCollection.collectionseq = result.getInt(10);
                        questionCollection.disregardcomplexitylevel = result.getBoolean(11);
                        questionCollection.deliverallquestions = result.getBoolean(12);
                        questionCollection.questionbreakup = (Integer[]) result.getArray(13).getArray();
                        questionCollection.collection = result.getString(14);
                        questionCollection.title = result.getString(15);
                        questionCollection.description = result.getString(16);
                        questionCollection.questions = new ArrayList<>();

                        for (int i = 0; i < questionCollection.questionsId.length; i++) {
                            stmt = con.prepareStatement("SELECT id,complexitylevel,questiontype,questionjson FROM "
                                    + schemaName
                                    + ".question WHERE id = ? ");
                            stmt.setInt(1, questionCollection.questionsId[i]);
                            questionResult = stmt.executeQuery();
                            while (questionResult.next()) {
                                com.brewconsulting.DB.masters.Question question = new Question();
                                question.id = questionResult.getInt(1);
                                question.complexityLevel = questionResult.getString(2);
                                question.questionType = questionResult.getString(3);
                                question.questionJson = questionResult.getString(4);
                                questionCollection.questions.add(question);
                            }
                        }
                        collectionsList.add(questionCollection);
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (questionResult != null)
                    if (!questionResult.isClosed())
                        questionResult.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return collectionsList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * This method is used to delete Question Collections from database
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteQuestionCollections(int id, boolean isGroup, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            ResultSet resultSet = null;

            try {

                if (isGroup) {
                    stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, id);
                    affectedRows = stmt.executeUpdate();
                } else {
                    stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, id);
                    affectedRows = stmt.executeUpdate();
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
            return affectedRows;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to remove question from question collections.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeQuestionFromCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet = null;
            Integer[] array = new Integer[3];

            try {
                if (con != null) {


/*
                    stmt = con.prepareStatement(" SELECT count(complexitylevel) FROM " + schemaName +
                            ".question WHERE complexitylevel = ? AND id = ? ");
*/

                    if (node.get("isGroup").asBoolean()) {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".groupsessioncontenttestquestioncollection SET questions = array_remove(questions, ? )"
                                        + " WHERE id = ?");
                        stmt.setInt(1, node.get("questionId").asInt());
                        stmt.setInt(2, node.get("collectionId").asInt());

                        result = stmt.executeUpdate();

                        stmt = con.prepareStatement("SELECT questionbreakup FROM "
                                + schemaName
                                + ".groupsessioncontenttestquestioncollection WHERE id = ? ");
                        stmt.setInt(1, node.get("collectionId").asInt());
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            array = (Integer[]) resultSet.getArray(1).getArray();
                        }

                        if (node.get("isDecrement").asBoolean()) {
                            String complexLevel = node.get("Level").asText();
                            Array arr = null;
                            if (complexLevel.equals("LOW")) {
                                array[0] = array[0] - 1;
                            }
                            if (complexLevel.equals("MEDIUM")) {
                                array[1] = array[1] - 1;
                            }
                            if (complexLevel.equals("HIGH")) {
                                array[2] = array[2] - 1;

                            }
                            arr = con.createArrayOf("int", array);
                            stmt = con.prepareStatement(" UPDATE " + schemaName +
                                    ".groupsessioncontenttestquestioncollection SET questionbreakup = ? " +
                                    " WHERE id = ? ");
                            stmt.setArray(1, arr);
                            stmt.setInt(2, node.get("collectionId").asInt());
                            stmt.executeUpdate();

                        }

                    } else {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".cyclemeetingsessioncontenttestquestioncollection SET questions = array_remove(questions, ? )"
                                        + " WHERE id = ?");
                        stmt.setInt(1, node.get("questionId").asInt());
                        stmt.setInt(2, node.get("collectionId").asInt());

                        result = stmt.executeUpdate();

                        stmt = con.prepareStatement("SELECT questionbreakup FROM "
                                + schemaName
                                + ".cyclemeetingsessioncontenttestquestioncollection WHERE id = ? ");
                        stmt.setInt(1, node.get("collectionId").asInt());
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            array = (Integer[]) resultSet.getArray(1).getArray();
                        }

                        if (node.get("isDecrement").asBoolean()) {
                            String complexLevel = node.get("Level").asText();
                            Array arr = null;
                            if (complexLevel.equals("LOW")) {
                                array[0] = array[0] - 1;
                            }
                            if (complexLevel.equals("MEDIUM")) {
                                array[1] = array[1] - 1;
                            }
                            if (complexLevel.equals("HIGH")) {
                                array[2] = array[2] - 1;
                            }
                            arr = con.createArrayOf("int", array);
                            stmt = con.prepareStatement(" UPDATE " + schemaName +
                                    ".cyclemeetingsessioncontenttestquestioncollection SET questionbreakup = ? WHERE id = ? ");
                            stmt.setArray(1, arr);
                            stmt.setInt(2, node.get("collectionId").asInt());
                            stmt.executeUpdate();
                        }
                    }

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
     * Method is used to append question in question collections.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int appendQuestionInCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet = null;
            int gCount = 0, mCount = 0;
            List<Integer> list = new ArrayList<>();

            try {
                if (con != null) {


                    for (int i = 0; i < node.withArray("questionId").size(); i++) {
                        if (node.get("isGroup").asBoolean()) {
                            stmt = con.prepareStatement(" SELECT count(*) FROM " +
                                    schemaName +
                                    ".groupsessioncontenttestquestioncollection " +
                                    " WHERE id = ? AND ? = ANY(questions :: int[])");
                            stmt.setInt(1, node.get("collectionId").asInt());
                            stmt.setInt(2, node.withArray("questionId").get(i).asInt());
                            resultSet = stmt.executeQuery();
                            while (resultSet.next()) {
                                gCount = resultSet.getInt(1);
                            }

                            if (gCount == 0) {
                                stmt = con
                                        .prepareStatement("UPDATE "
                                                + schemaName
                                                + ".groupsessioncontenttestquestioncollection SET questions = array_append(questions, ? )"
                                                + " WHERE id = ?");

                                stmt.setInt(1, node.withArray("questionId").get(i).asInt());
                                stmt.setInt(2, node.get("collectionId").asInt());

                                result = stmt.executeUpdate();
                                list.add(result);
                            } else {
                                result = result + 0;
                            }
                        } else {
                            stmt = con.prepareStatement(" SELECT count(*) FROM " +
                                    schemaName +
                                    ".cyclemeetingsessioncontenttestquestioncollection " +
                                    " WHERE id = ? AND ? = ANY(questions :: int[])");
                            stmt.setInt(1, node.get("collectionId").asInt());
                            stmt.setInt(2, node.withArray("questionId").get(i).asInt());
                            resultSet = stmt.executeQuery();
                            while (resultSet.next()) {
                                mCount = resultSet.getInt(1);
                            }

                            if (mCount == 0) {
                                stmt = con
                                        .prepareStatement("UPDATE "
                                                + schemaName
                                                + ".cyclemeetingsessioncontenttestquestioncollection SET questions = array_append(questions, ? )"
                                                + " WHERE id = ?");

                                stmt.setInt(1, node.withArray("questionId").get(i).asInt());
                                stmt.setInt(2, node.get("collectionId").asInt());

                                result = stmt.executeUpdate();
                                list.add(result);
                            } else {
                                result = result + 0;
                            }
                        }
                    }
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
            return list.size();
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to update collection Details.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateCollections(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int affectedRow;
            Array quesBreak = null;

            try {
                if (con != null) {
                    if (node.get("isGroup").asBoolean()) {
                        if (node.get("deliverallquestions").asBoolean()) {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, deliverallquestions = ? "
                                            + " WHERE id = ? ");

                        } else {
                            Integer[] quesbrkArr = new Integer[node.withArray("questionbreakup").size()];

                            // Convert JsonArray into String Array
                            for (int i = 0; i < node.withArray("questionbreakup").size(); i++) {
                                quesbrkArr[i] = node.withArray("questionbreakup").get(i).asInt();
                            }

                            quesBreak = con.createArrayOf("int", quesbrkArr);

                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, deliverallquestions = ?,"
                                            + " questionbreakup = ?,disregardcomplexitylevel = ?"
                                            + " WHERE id = ? ");
                        }
                        stmt.setString(1, node.get("collectionname").asText());
                        stmt.setBoolean(2, node.get("deliverallquestions").asBoolean());

                        if (!node.get("deliverallquestions").asBoolean()) {
                            stmt.setArray(3, quesBreak);
                            stmt.setBoolean(4, node.get("disregardcomplexitylevel").asBoolean());
                            stmt.setInt(5, node.get("collectionId").asInt());
                        } else
                            stmt.setInt(3, node.get("collectionId").asInt());

                        affectedRow = stmt.executeUpdate();
                    } else {
                        if (node.get("deliverallquestions").asBoolean()) {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, deliverallquestions = ? "
                                            + " WHERE id = ? ");

                        } else {
                            Integer[] quesbrkArr = new Integer[node.withArray("questionbreakup").size()];

                            // Convert JsonArray into String Array
                            for (int i = 0; i < node.withArray("questionbreakup").size(); i++) {
                                quesbrkArr[i] = node.withArray("questionbreakup").get(i).asInt();
                            }

                            quesBreak = con.createArrayOf("int", quesbrkArr);

                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, deliverallquestions = ?,"
                                            + " questionbreakup = ?,disregardcomplexitylevel = ?"
                                            + " WHERE id = ? ");
                        }
                        stmt.setString(1, node.get("collectionname").asText());
                        stmt.setBoolean(2, node.get("deliverallquestions").asBoolean());

                        if (!node.get("deliverallquestions").asBoolean()) {
                            stmt.setArray(3, quesBreak);
                            stmt.setBoolean(4, node.get("disregardcomplexitylevel").asBoolean());
                            stmt.setInt(5, node.get("collectionId").asInt());
                        } else
                            stmt.setInt(3, node.get("collectionId").asInt());

                        affectedRow = stmt.executeUpdate();
                    }

                    arrangeQuestions(node.get("agendaId").asInt(), loggedInUser, node.get("isGroup").asBoolean(), node.get("meetingId").asInt());
                } else
                    throw new Exception("DB connection is null");

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
            return affectedRow;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method is used to set Collection's setting details.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateSettings(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int affectedRow = 0;
            try {

                if (con != null) {


                    Array correctScoreArray = null;
                    Array inCorrectScoreArray = null;
                    Array diffTimeArray = null;

                    if (node.get("isGroup").asBoolean()) {

                        stmt = con.prepareStatement(" UPDATE "
                                + schemaName
                                + ".groupsessioncontenttest SET testinstruction = ?, testendnote = ?,testdescription=?,"
                                + " showfeedback = ?, allowreview = ?, applyscoring = ?, scorecorrect = ?,scoreincorrect = ?,"
                                + " applytimeperquestion = ?, timeperquestion= ?, duration = CAST (? AS INTERVAL),randomdelivery = ? "
                                + " WHERE agendaid = ? ");

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

                        stmt.setBoolean(4, node.get("showFeedBack").asBoolean());
                        stmt.setBoolean(5, node.get("AllowReview").asBoolean());
                        stmt.setBoolean(6, node.get("Scoring").get("IsApplyScoring").asBoolean());

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


                        stmt.setArray(7, correctScoreArray);
                        stmt.setArray(8, inCorrectScoreArray);

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

                        stmt.setArray(10, diffTimeArray);

                        if (node.get("TimeLimitation").has("FixedTime"))
                            stmt.setObject(11, node.get("TimeLimitation").get("FixedTime").asText());
                        else
                            stmt.setObject(11, "00:00");

                        stmt.setBoolean(12,node.get("randomDelivery").asBoolean());

                        stmt.setInt(13, node.get("agendaId").asInt());

                        affectedRow = stmt.executeUpdate();

                    } else {

                        DeliveryMode mode = DeliveryMode.valueOf(node.get("deliveryMode").asText().toUpperCase());

                        stmt = con.prepareStatement(" UPDATE "
                                + schemaName
                                + ".cyclemeetingsessioncontenttest SET testinstruction = ?, testendnote = ?,testdescription=?,"
                                + " showfeedback = ?, allowreview = ?, applyscoring = ?, scorecorrect = ?,scoreincorrect = ?,"
                                + " applytimeperquestion = ?, timeperquestion= ?, duration = CAST (? AS INTERVAL) ,"
                                + " deliverymode = CAST(? AS master.deliveryformat),randomdelivery = ? "
                                + " WHERE agendaid = ? ");

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

                        stmt.setBoolean(4, node.get("showFeedBack").asBoolean());
                        stmt.setBoolean(5, node.get("AllowReview").asBoolean());
                        stmt.setBoolean(6, node.get("Scoring").get("IsApplyScoring").asBoolean());

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


                        stmt.setArray(7, correctScoreArray);
                        stmt.setArray(8, inCorrectScoreArray);

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

                        stmt.setArray(10, diffTimeArray);

                        if (node.get("TimeLimitation").has("FixedTime"))
                            stmt.setObject(11, node.get("TimeLimitation").get("FixedTime").asText());
                        else
                            stmt.setObject(11, "00:00");

                        stmt.setString(12, mode.name());

                        stmt.setBoolean(13,node.get("randomDelivery").asBoolean());

                        stmt.setInt(14, node.get("agendaId").asInt());

                        affectedRow = stmt.executeUpdate();

                    }

                    arrangeQuestions(node.get("agendaId").asInt(), loggedInUser, node.get("isGroup").asBoolean(), node.get("meetingId").asInt());

                } else
                    throw new Exception("DB connection is null");

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
            return affectedRow;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method used to get All collection's Setting details.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getSettingDetails(int agendaId, boolean isGroup, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int affectedRow = 0;
            List<QuestionCollection> collectionList = new ArrayList<>();
            QuestionCollection questionCollection;
            try {

                if (isGroup) {
                    stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                            " updateon, updatedby, testinstruction, testendnote,testdescription, applyscoring, " +
                            " scorecorrect, scoreincorrect, showfeedback, duration, " +
                            " timeperquestion, applytimeperquestion, allowreview, title, description,randomdelivery " +
                            " FROM " + schemaName + ".groupsessioncontenttest " +
                            " WHERE  agendaid = ? ");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        questionCollection = new QuestionCollection();
                        questionCollection.id = resultSet.getInt(1);
                        questionCollection.agendaId = resultSet.getInt(2);
                        questionCollection.contentType = resultSet.getString(3);
                        questionCollection.contentSeq = resultSet.getInt(4);
                        questionCollection.createdOn = resultSet.getTimestamp(5);
                        questionCollection.createBy = resultSet.getInt(6);
                        questionCollection.updateOn = resultSet.getTimestamp(7);
                        questionCollection.updateBy = resultSet.getInt(8);
                        questionCollection.Instrction = resultSet.getString(9);
                        questionCollection.EndNote = resultSet.getString(10);
                        questionCollection.Description = resultSet.getString(11);

                        questionCollection.Scoring = new HashMap();
                        questionCollection.Scoring.put("IsApplyScoring", resultSet.getBoolean(12));

                        HashMap CorrectScore = new HashMap();
                        Integer[] corrArray = (Integer[]) resultSet.getArray(13).getArray();
                        if (corrArray.length > 0 && corrArray.length == 3) {
                            CorrectScore.put("Low", corrArray[0]);
                            CorrectScore.put("Medium", corrArray[1]);
                            CorrectScore.put("High", corrArray[2]);
                        }

                        questionCollection.Scoring.put("CorrectScore", CorrectScore);
                        HashMap IncorrectScore = new HashMap();
                        Double[] inCorrArray = (Double[]) resultSet.getArray(14).getArray();
                        if (inCorrArray.length > 0 && inCorrArray.length == 3) {
                            IncorrectScore.put("Low", inCorrArray[0]);
                            IncorrectScore.put("Medium", inCorrArray[1]);
                            IncorrectScore.put("High", inCorrArray[2]);
                        }

                        questionCollection.Scoring.put("IncorrectScore", IncorrectScore);

                        questionCollection.TimeLimitation = new HashMap();
                        questionCollection.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(18));
                        questionCollection.TimeLimitation.put("FixedTime", resultSet.getString(16));

                        questionCollection.showFeedBack = resultSet.getBoolean(15);

                        HashMap DifferentTime = new HashMap();
                        Integer[] diffArr = (Integer[]) resultSet.getArray(17).getArray();
                        if (diffArr.length > 0 && diffArr.length == 3) {
                            DifferentTime.put("Low", diffArr[0]);
                            DifferentTime.put("Medium", diffArr[1]);
                            DifferentTime.put("High", diffArr[2]);
                        }

                        questionCollection.TimeLimitation.put("DifferentTime", DifferentTime);
                        questionCollection.AllowReview = resultSet.getBoolean(19);
                        questionCollection.title = resultSet.getString(20);
                        questionCollection.description = resultSet.getString(21);
                        questionCollection.randomdelivery = resultSet.getBoolean(22);

                        collectionList.add(questionCollection);
                    }
                } else {
                    stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                            " updateon, updatedby, testinstruction, testendnote,testdescription, applyscoring, " +
                            " scorecorrect, scoreincorrect, showfeedback, duration, " +
                            " timeperquestion, applytimeperquestion, allowreview, title, description,deliverymode,randomdelivery " +
                            " FROM " + schemaName + ".cyclemeetingsessioncontenttest " +
                            " WHERE  agendaid = ? ");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        questionCollection = new QuestionCollection();
                        questionCollection.id = resultSet.getInt(1);
                        questionCollection.agendaId = resultSet.getInt(2);
                        questionCollection.contentType = resultSet.getString(3);
                        questionCollection.contentSeq = resultSet.getInt(4);
                        questionCollection.createdOn = resultSet.getTimestamp(5);
                        questionCollection.createBy = resultSet.getInt(6);
                        questionCollection.updateOn = resultSet.getTimestamp(7);
                        questionCollection.updateBy = resultSet.getInt(8);
                        questionCollection.Instrction = resultSet.getString(9);
                        questionCollection.EndNote = resultSet.getString(10);
                        questionCollection.Description = resultSet.getString(11);

                        questionCollection.Scoring = new HashMap();
                        questionCollection.Scoring.put("IsApplyScoring", resultSet.getBoolean(12));

                        HashMap CorrectScore = new HashMap();
                        Integer[] corrArray = (Integer[]) resultSet.getArray(13).getArray();
                        if (corrArray.length > 0 && corrArray.length == 3) {
                            CorrectScore.put("Low", corrArray[0]);
                            CorrectScore.put("Medium", corrArray[1]);
                            CorrectScore.put("High", corrArray[2]);
                        }

                        questionCollection.Scoring.put("CorrectScore", CorrectScore);

                        HashMap IncorrectScore = new HashMap();
                        Double[] inCorrArray = (Double[]) resultSet.getArray(14).getArray();
                        if (inCorrArray.length > 0 && inCorrArray.length == 3) {
                            IncorrectScore.put("Low", inCorrArray[0]);
                            IncorrectScore.put("Medium", inCorrArray[1]);
                            IncorrectScore.put("High", inCorrArray[2]);
                        }

                        questionCollection.Scoring.put("IncorrectScore", IncorrectScore);

                        questionCollection.TimeLimitation = new HashMap();
                        questionCollection.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(18));
                        questionCollection.TimeLimitation.put("FixedTime", resultSet.getString(16));

                        questionCollection.showFeedBack = resultSet.getBoolean(15);

                        HashMap DifferentTime = new HashMap();
                        Integer[] diffArr = (Integer[]) resultSet.getArray(17).getArray();
                        if (diffArr.length > 0 && diffArr.length == 3) {
                            DifferentTime.put("Low", diffArr[0]);
                            DifferentTime.put("Medium", diffArr[1]);
                            DifferentTime.put("High", diffArr[2]);
                        }

                        questionCollection.TimeLimitation.put("DifferentTime", DifferentTime);
                        questionCollection.AllowReview = resultSet.getBoolean(19);
                        questionCollection.title = resultSet.getString(20);
                        questionCollection.description = resultSet.getString(21);
                        questionCollection.deliveryMode = resultSet.getString(22);
                        questionCollection.randomdelivery = resultSet.getBoolean(23);

                        collectionList.add(questionCollection);
                    }
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
            return collectionList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  Method is used to get question set.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getQuesionSet_bk(int agendaId, boolean isGroup, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            PreparedStatement stmt = null;
            Connection con = DBConnectionProvider.getConn();
            ResultSet resultSet = null;
            List<QuestionCollection> lstCollection = new ArrayList<>();
            lstCollection = getAllQuestionCollection(agendaId, isGroup, loggedInUser);

            List<QuestionCollection> questionCollectionList = new ArrayList<>();
            boolean isValid = true;

            try {
                if (con != null) {
                    if (isValid) {

                        if (isGroup) {
                            stmt = con.prepareStatement(" SELECT sessionname, testinstruction, testendnote, testdescription," +
                                    " allowreview, timeperquestion,applytimeperquestion, duration " +
                                    " FROM " + schemaName + ".groupagenda g " +
                                    " left join " + schemaName + ".groupsessioncontenttest t on t.agendaid = g.id " +
                                    " WHERE t.agendaid = ? ");
                        } else {
                            stmt = con.prepareStatement(" SELECT sessionname, testinstruction, testendnote, testdescription," +
                                    " allowreview, timeperquestion,applytimeperquestion, duration " +
                                    " FROM " + schemaName + ".cyclemeetingagenda g " +
                                    " left join " + schemaName + ".cyclemeetingsessioncontenttest t on t.agendaid = g.id " +
                                    " WHERE t.agendaid = ? ");
                        }
                        stmt.setInt(1, agendaId);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            QuestionCollection collection = new QuestionCollection();

                            collection.agendaName = resultSet.getString(1);
                            collection.Instrction = resultSet.getString(2);
                            collection.EndNote = resultSet.getString(3);
                            collection.Description = resultSet.getString(4);
                            collection.AllowReview = resultSet.getBoolean(5);

                            collection.TimeLimitation = new HashMap();
                            collection.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(7));
                            collection.TimeLimitation.put("FixedTime", resultSet.getString(8));

                            HashMap DifferentTime = new HashMap();
                            Integer[] diffArr = (Integer[]) resultSet.getArray(6).getArray();
                            if (diffArr.length > 0 && diffArr.length == 3) {
                                DifferentTime.put("Low", diffArr[0]);
                                DifferentTime.put("Medium", diffArr[1]);
                                DifferentTime.put("High", diffArr[2]);
                            }

                            collection.TimeLimitation.put("DifferentTime", DifferentTime);

//                                questionCollectionList.add(collection);
                            collection.questions = new ArrayList<>();

                            if (lstCollection != null && lstCollection.size() > 0) {
                                for (QuestionCollection qc : lstCollection) {
                                    if (qc.deliverallquestions) {
                                        for (int index = 0; index < qc.questionsId.length; index++) {
                                            collection.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(qc.questionsId[index], loggedInUser));
                                        }
                                    } else if (!qc.deliverallquestions && !qc.disregardcomplexitylevel) {
                                        int low = qc.questionbreakup[0];
                                        int medium = qc.questionbreakup[1];
                                        int high = qc.questionbreakup[2];
                                        List<Question> lstTemp = new ArrayList<>();

                                        if (low > 0) {
                                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "LOW");
                                            //Collections.shuffle(lstTemp);
                                            for (int i = 0; i < low; i++) {
                                                collection.questions.add(lstTemp.get(i));
                                            }
                                            lstTemp.clear();
                                        }
                                        if (medium > 0) {
                                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "MEDIUM");
                                            //Collections.shuffle(lstTemp);
                                            for (int i = 0; i < medium; i++) {
                                                collection.questions.add(lstTemp.get(i));
                                            }
                                            lstTemp.clear();
                                        }
                                        if (high > 0) {
                                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "HIGH");
                                            //Collections.shuffle(lstTemp);
                                            for (int i = 0; i < high; i++) {
                                                collection.questions.add(lstTemp.get(i));
                                            }
                                            lstTemp.clear();
                                        }

                                    } else if (!qc.deliverallquestions && qc.disregardcomplexitylevel) {
                                        List<Question> lstTemp = new ArrayList<>();
                                        int number = qc.questionbreakup[0];
                                        if (number > 0) {
                                            if (qc.questionsId.length >= number) {
                                                for (int i = 0; i < number; i++) {
                                                    collection.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(qc.questionsId[i], loggedInUser));
                                                }
                                            } else {
                                                for (int i = 0; i < qc.questionsId.length; i++) {
                                                    collection.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(qc.questionsId[i], loggedInUser));
                                                }
                                            }
                                        }
                                    }

                                    if (qc.randomdelivery) {
                                        Collections.shuffle(collection.questions);
                                    }
                                }
                                questionCollectionList.add(collection);
                            } else {

                            }
                        }
                    } else {

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
            return questionCollectionList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param sessionId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getQuestionSet(int sessionId, LoggedInUser loggedInUser) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        List<QuestionCollection> collectionList = new ArrayList<>();
        ResultSet resultSet = null;
        String schemaname = loggedInUser.schemaName;
        int meetingId = 0;
        boolean isStartSession = false, isPresent = false, isRandom = false, isAppMode = false;
        Integer[] quesArray = new Integer[0];
        String roleName = loggedInUser.roles.get(0).roleName;

        try {
            stmt = con.prepareStatement(" SELECT c.id FROM " + schemaname + ".cyclemeeting c " +
                    " left join " + schemaname + ".cyclemeetingagenda c1 on c1.cyclemeetingid = c.id" +
                    " WHERE c1.id = ? ");

            stmt.setInt(1, sessionId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                meetingId = resultSet.getInt(1);
            }

            System.out.println(" Meeting Id : " + meetingId);

            stmt = con.prepareStatement(" SELECT sessionstarttime, sessionendtime FROM " + schemaname + ".cyclemeetingactualtimes" +
                    " WHERE sessionid = ? AND cycleMeetingid = ? ");
            stmt.setInt(1, sessionId);
            stmt.setInt(2, meetingId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                System.out.println("Start Time : " + resultSet.getTimestamp(1));
                System.out.println("ENd TIme : " + resultSet.getTimestamp(2));
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
                String date = sd.format(resultSet.getTimestamp(2));
                System.out.println("DAte : " + date);

                if (resultSet.getTimestamp(1) != null && date.equals("1970-01-01")) {
                    isStartSession = true;
                }
            }

            stmt = con.prepareStatement(" SELECT count(*) as Count FROM " + schemaname + ".cyclemeetingattendance " +
                    "  WHERE cyclemeetingid = ? AND ? = ANY(userid :: int[])");
            stmt.setInt(1, meetingId);
            stmt.setInt(2, loggedInUser.id);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getInt("Count") > 0) {
                    isPresent = true;
                }
            }

            if ((isStartSession && isPresent) || roleName.equals("ROOT") || roleName.equals("MANAGEMENT")) {
                stmt = con.prepareStatement(" SELECT questionids,israndom " +
                        " FROM " + schemaname + ".cyclemeetingassessmentactual WHERE meetingid = ? AND sessionid = ?");
                stmt.setInt(1, meetingId);
                stmt.setInt(2, sessionId);
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    quesArray = (Integer[]) resultSet.getArray(1).getArray();
                }

                stmt = con.prepareStatement(" SELECT sessionname, testinstruction, testendnote, testdescription," +
                        " allowreview, timeperquestion,applytimeperquestion, duration, deliverymode,randomdelivery " +
                        " FROM " + schemaname + ".cyclemeetingagenda g " +
                        " left join " + schemaname + ".cyclemeetingsessioncontenttest t on t.agendaid = g.id " +
                        " WHERE t.agendaid = ? ");

                stmt.setInt(1, sessionId);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    QuestionCollection collection = new QuestionCollection();

                    collection.agendaName = resultSet.getString(1);
                    collection.Instrction = resultSet.getString(2);
                    collection.EndNote = resultSet.getString(3);
                    collection.Description = resultSet.getString(4);
                    collection.AllowReview = resultSet.getBoolean(5);

                    collection.TimeLimitation = new HashMap();
                    collection.TimeLimitation.put("IsApplyTimePerQuestion", resultSet.getBoolean(7));
                    collection.TimeLimitation.put("FixedTime", resultSet.getString(8));

                    HashMap DifferentTime = new HashMap();
                    Integer[] diffArr = (Integer[]) resultSet.getArray(6).getArray();
                    if (diffArr.length > 0 && diffArr.length == 3) {
                        DifferentTime.put("Low", diffArr[0]);
                        DifferentTime.put("Medium", diffArr[1]);
                        DifferentTime.put("High", diffArr[2]);
                    }

                    collection.TimeLimitation.put("DifferentTime", DifferentTime);
                    collection.deliveryMode = resultSet.getString(9);

                    if (resultSet.getString(9).equals("APP")) {
                        isAppMode = true;
                    }

                    collection.randomdelivery = resultSet.getBoolean(10);
                    isRandom = resultSet.getBoolean(10);

///                   questionCollectionList.add(collection);
                    collection.questions = new ArrayList<>();

                    System.out.println("Length : " + quesArray.length);
                    for (int i = 0; i < quesArray.length; i++) {
                        System.out.println("Array " + i + " : " + quesArray[i]);
                        collection.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(quesArray[i], loggedInUser));
                    }
                    collectionList.add(collection);
                }
                if (isRandom && isAppMode) {
                    Collections.shuffle(collectionList);
                }
            } else {
                throw new BadRequestException(" You are not authorized for this test right now. ");
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

    /***
     *
     *
     * @param agendaId
     * @param loggedInUser
     * @param isGroup
     * @param meetingId
     * @throws Exception
     */
    public static void arrangeQuestions(int agendaId, LoggedInUser loggedInUser, boolean isGroup, int meetingId) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> questionIds = new ArrayList<>();
        boolean isRandom = false;
        try {
            String schemaName = loggedInUser.schemaName;
            List<QuestionCollection> lstCollection;
            lstCollection = getAllQuestionCollection(agendaId, isGroup, loggedInUser);
            System.out.println(" List Size : " + lstCollection.size());
            if (lstCollection != null && lstCollection.size() > 0) {
                for (QuestionCollection qc : lstCollection) {
                    if (qc.deliverallquestions) {
                        for (int index = 0; index < qc.questionsId.length; index++) {
                            questionIds.add(qc.questionsId[index]);
                            //collection.questions.add(com.brewconsulting.DB.masters.Question.getQuestionById(qc.questionsId[index], loggedInUser));
                        }
                    } else if (!qc.deliverallquestions && !qc.disregardcomplexitylevel) {
                        int low = qc.questionbreakup[0];
                        int medium = qc.questionbreakup[1];
                        int high = qc.questionbreakup[2];
                        List<Question> lstTemp = new ArrayList<>();

                        if (low > 0) {
                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "LOW");
                            for (int i = 0; i < low; i++) {
                                questionIds.add(lstTemp.get(i).id);
                            }
                            lstTemp.clear();
                        }
                        if (medium > 0) {
                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "MEDIUM");
                            for (int i = 0; i < medium; i++) {
                                questionIds.add(lstTemp.get(i).id);
                            }
                            lstTemp.clear();
                        }
                        if (high > 0) {
                            lstTemp = com.brewconsulting.DB.masters.Question.getQuestionsByListAndComplexity(qc.questionsId, "HIGH");
                            for (int i = 0; i < high; i++) {
                                questionIds.add(lstTemp.get(i).id);
                            }
                            lstTemp.clear();
                        }

                    } else if (!qc.deliverallquestions && qc.disregardcomplexitylevel) {
                        List<Question> lstTemp = new ArrayList<>();
                        int number = qc.questionbreakup[0];
                        if (number > 0) {
                            if (qc.questionsId.length >= number) {
                                for (int i = 0; i < number; i++) {
                                    questionIds.add(qc.questionsId[i]);
                                }
                            } else {
                                for (int i = 0; i < qc.questionsId.length; i++) {
                                    questionIds.add(qc.questionsId[i]);
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("ID Size : " + questionIds.size());
            if (questionIds.size() > 0) {
                Integer[] quesId = new Integer[questionIds.size()];
                for (int i = 0; i < questionIds.size(); i++) {
                    quesId[i] = questionIds.get(i);
                }

                Array quesArr = con.createArrayOf("int", quesId);

                stmt = con.prepareStatement("SELECT id,questionids FROM " + schemaName + ".cyclemeetingassessmentactual" +
                        " WHERE meetingid = ? AND sessionid = ? ");
                stmt.setInt(1, meetingId);
                stmt.setInt(2, agendaId);
                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    System.out.println(" In IF ...");
                    stmt = con.prepareStatement(" INSERT  INTO " + schemaName
                            + ".cyclemeetingassessmentactual(meetingid, groupagenda, sessionid, questionids,israndom)" +
                            " VALUES (?, ?, ?, ?,?)");
                    stmt.setInt(1, meetingId);
                    stmt.setBoolean(2, isGroup);
                    stmt.setInt(3, agendaId);
                    stmt.setArray(4, quesArr);
                    stmt.setBoolean(5, isRandom);

                    stmt.executeUpdate();
                } else {
                    stmt = con.prepareStatement(" UPDATE " + schemaName + ".cyclemeetingassessmentactual SET questionids = ?, israndom = ? " +
                            " WHERE  id = ? ");
                    stmt.setArray(1, quesArr);
                    stmt.setBoolean(2, isRandom);
                    stmt.setInt(3, resultSet.getInt(1));

                    stmt.executeUpdate();
                }
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
    }

    /***
     *
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addUsersAllAnswer(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
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

                        if(isReviewQuestion) {
                            stmt.setDouble(6, 0);
                            stmt.setBoolean(9, true);

                            if(String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).isEmpty() ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals(null) ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals("")){
                                stmt.setBoolean(8,false);
                            }else{
                                stmt.setBoolean(8,true);
                            }
                        } else{
                            stmt.setBoolean(9, false);
                            if(String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).isEmpty() ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals(null) ||
                                    String.valueOf(node.withArray("answerSet").get(i).get("answerJson")).equals("")){
                                stmt.setDouble(6, 0);
                                stmt.setBoolean(8,false);
                            }else {
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
     *
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addUsersAnswer(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
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
            String QuestionJson = "" , complexitylevel ="";
            boolean isReviewQuestion = false;
            String UserAnswerJson = String.valueOf(node.get("answerJson"));

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
                        complexitylevel = resultSet.getString(2);
                        QuestionJson = resultSet.getString(3);
                        actualAnswer = resultSet.getString(4);
                        isReviewQuestion = resultSet.getBoolean(5);


                        stmt = con.prepareStatement(" SELECT count(*) AS Count FROM "+schemaname+".cyclemeetingassessmentresult" +
                                " WHERE agendaid = ? AND userid = ? AND questionid = ? ");
                        stmt.setInt(1,node.get("agendaId").asInt());
                        stmt.setInt(2,node.get("userId").asInt());
                        stmt.setInt(3,node.get("questionId").asInt());
                        updateResultSet = stmt.executeQuery();
                        while (updateResultSet.next())
                        {
                            if(updateResultSet.getInt("Count") > 0)
                            {
                                stmt = con.prepareStatement(" UPDATE "+schemaname+".cyclemeetingassessmentresult " +
                                        " SET answerjson = ?, questionjson = ?, useranswerjson = ?, score = ?,isattemp = ? ,isreview = ?" +
                                        " WHERE agendaid = ? AND userid = ? AND questionid = ?");
                                stmt.setString(1, actualAnswer);
                                stmt.setString(2, QuestionJson);
                                stmt.setString(3, UserAnswerJson );

                                if(isReviewQuestion) {
                                    stmt.setDouble(4, 0);
                                    stmt.setBoolean(6, true);

                                    if(String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")){
                                        stmt.setBoolean(5,false);
                                    }else{
                                        stmt.setBoolean(5,true);
                                    }
                                } else{
                                    stmt.setBoolean(6, false);
                                    if(String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")){
                                        stmt.setDouble(4, 0);
                                        stmt.setBoolean(5,false);
                                    }else {
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
                                        }else{
                                            stmt.setDouble(4, 0);
                                            stmt.setBoolean(5, true);
                                        }
                                    }
                                }

                                stmt.setInt(7,node.get("agendaId").asInt());
                                stmt.setInt(8,node.get("userId").asInt());
                                stmt.setInt(9,node.get("questionId").asInt());
                            }
                            else
                            {
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

                                if(isReviewQuestion) {
                                    stmt.setDouble(6, 0);
                                    stmt.setBoolean(9, true);

                                    if(String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")){
                                        stmt.setBoolean(8,false);
                                    }else{
                                        stmt.setBoolean(8,true);
                                    }
                                } else{
                                    stmt.setBoolean(9, false);
                                    if(String.valueOf(node.get("answerJson")).isEmpty() ||
                                            String.valueOf(node.get("answerJson")).equals(null) ||
                                            String.valueOf(node.get("answerJson")).equals("")){
                                        stmt.setDouble(6, 0);
                                        stmt.setBoolean(8,false);
                                    }else {
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
                                        }else{
                                            stmt.setDouble(6, 0);
                                            stmt.setBoolean(8, true);
                                        }
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
     *  This method is used to Get User's Total Score.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getUsersWiseResult(int agendaId,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            List<QuestionCollection> questionList = new ArrayList<>();

            try {
                if(con != null) {
                    stmt = con.prepareStatement(
                            " SELECT count(case when answerjson = useranswerjson and isattemp = true and isreview = false then 1 else null end) as CorrectAnswer," +
                                  " count(case when isattemp = true and isreview = false and answerjson != useranswerjson then 1 else null end) as IncorrectAnswer,"+
                                    "count(case when isattemp = false then 1 else null end) as notAttempt,"+
                                    " sum(score) as TotalScore, userid,username,firstname,lastname" +
                                    " FROM " + schemaname + ".cyclemeetingassessmentresult " +
                                     " left join master.users u on u.id = userid " +
                                     " WHERE agendaid = ? GROUP BY(userid,u.username,u.firstname,u.lastname) ORDER BY totalscore DESC ");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        QuestionCollection collection = new QuestionCollection();
                        collection.correctAnswer = resultSet.getInt(1);
                        collection.inCorrectAnswer = resultSet.getInt(2);
                        collection.notAttempt = resultSet.getInt(3);
                        collection.score = resultSet.getDouble(4);
                        collection.userId = resultSet.getInt(5);
                        collection.username = resultSet.getString(6);
                        collection.fullname = resultSet.getString(7) + " "+ resultSet.getString(8);
                        questionList.add(collection);
                    }
                }
                else
                    throw new Exception("DB connection is null");
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  This method is used to Get User's Total Score.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getQuestionWiseResult(int agendaId,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaname = loggedInUser.schemaName;
            ResultSet resultSet = null;
            List<QuestionCollection> questionList = new ArrayList<>();

            try {
                if(con != null) {
                    stmt = con.prepareStatement(
                            " SELECT count(case when c.answerjson = useranswerjson and c.isattemp = true and c.isreview = false then 1 else null end) as CorrectAnswer," +
                                    " count(case when c.isattemp = true and c.isreview = false and c.answerjson != useranswerjson then 1 else null end) as IncorrectAnswer,"+
                                    " count(case when c.isattemp = false then 1 else null end) as notAttempt,"+
                                    " questionid,c.questionjson,c.answerjson,q.isreview " +
                                    " FROM " + schemaname + ".cyclemeetingassessmentresult c " +
                                    " left join "+schemaname+".question q ON q.id = questionid " +
                                    " WHERE agendaid = ? GROUP BY(questionid,c.questionjson,c.answerjson,q.isreview)");
                    stmt.setInt(1, agendaId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        QuestionCollection collection = new QuestionCollection();
                        collection.correctAnswer = resultSet.getInt(1);
                        collection.inCorrectAnswer = resultSet.getInt(2);
                        collection.notAttempt = resultSet.getInt(3);
                        collection.questionId = resultSet.getInt(4);
                        collection.questionJson= resultSet.getString(5);
                        collection.answerJson = resultSet.getString(6);
                        collection.isReview = resultSet.getBoolean(7);
                        questionList.add(collection);
                    }
                }
                else
                    throw new Exception("DB connection is null");
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  Method is used to get Specific Question wise user's Score.
     *
     * @param userId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getUsersQuestionWiseScore(int userId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write") )
        {
            Connection con = DBConnectionProvider.getConn();
            ArrayList<QuestionCollection> questionList = new ArrayList<>();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            String schemaname = loggedInUser.schemaName;

            try {
                if(con != null)
                {
                    stmt = con.prepareStatement(" SELECT questionid,r.questionjson,score,userid,username " +
                            " FROM "+schemaname+".cyclemeetingassessmentresult r" +
                            " left join "+schemaname+".question q on q.id = questionid" +
                            " left join master.users u on u.id = userid" +
                            " WHERE userid = ? ");
                    stmt.setInt(1,userId);
                    resultSet = stmt.executeQuery();

                    while (resultSet.next())
                    {
                        QuestionCollection collection = new QuestionCollection();
                        collection.questionId = resultSet.getInt(1);
                        collection.questionJson = resultSet.getString(2);
                        collection.score = resultSet.getDouble(3);
                        collection.userId = resultSet.getInt(4);
                        collection.username = resultSet.getString(5);
                        questionList.add(collection);
                    }
                }
                else
                    throw new Exception("DB connection is null");
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getUserWiseParticularScore(int userId, int agendaId , String mode,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<QuestionCollection> questionsList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            String condition = "";
            try {
                if(mode.equals("correct")){
                    condition = " AND r.answerjson = useranswerjson AND isattemp = true AND r.isReview=false";
                }else if(mode.equals("incorrect")){
                    condition = " AND isattemp = true AND r.answerjson != useranswerjson AND r.isReview=false";
                }else if(mode.equals("notattempt")){
                    condition = " AND isattemp = false";
                }

                stmt = con.prepareStatement(" SELECT questionid,r.questionjson,r.answerjson,score," +
                        " q.imageurl,q.filetype,r.useranswerjson " +
                        " FROM "+schemaname+".cyclemeetingassessmentresult r " +
                        " left join "+schemaname+".question q on q.id = questionid " +
                        " WHERE userid = ? AND agendaid = ? " + condition);
                System.out.println("Condition : "+ condition);
                stmt.setInt(1,userId);
                stmt.setInt(2,agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next())
                {
                    QuestionCollection collection = new QuestionCollection();
                    collection.questionId = resultSet.getInt(1);
                    collection.questionJson = resultSet.getString(2);
                    collection.answerJson = resultSet.getString(3);
                    collection.score = resultSet.getDouble(4);
                    collection.imageURL = resultSet.getString(5);
                    collection.fileType = resultSet.getString(6);
                    collection.userAnswerJson = resultSet.getString(7);
                    questionsList.add(collection);
                }
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionsList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getQuestionWiseUserStatus(int questionId, int agendaId , String mode,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<QuestionCollection> questionsList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;
            String condition = "";
            try {
                if(mode.equals("correct")){
                    condition = "AND r.answerjson = useranswerjson AND isattemp = true AND isReview=false";
                }else if(mode.equals("incorrect")){
                    condition = "AND isattemp = true AND r.answerjson != useranswerjson AND isReview=false";
                }else if(mode.equals("notattempt")){
                    condition = "AND isattemp = false";
                }

                stmt = con.prepareStatement(" SELECT r.userid,score,u.username,u.firstname,u.lastname " +
                        " FROM "+schemaname+".cyclemeetingassessmentresult r " +
                        " left join master.users u on u.id = r.userid " +
                        " WHERE questionid = ? AND agendaid = ? " + condition);
                stmt.setInt(1,questionId);
                stmt.setInt(2,agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next())
                {
                    QuestionCollection collection = new QuestionCollection();
                    collection.questionId = resultSet.getInt(1);
                    collection.score = resultSet.getDouble(2);
                    collection.username = resultSet.getString(3);
                    collection.fullname = resultSet.getString(4)+ " " + resultSet.getString(5);
                    questionsList.add(collection);
                }
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionsList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getUserTotalScore(int userId,int agendaId,LoggedInUser loggedInUser) throws Exception
    {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            List<QuestionCollection> questionsList = new ArrayList<>();
            String schemaname = loggedInUser.schemaName;

            try {
                stmt = con.prepareStatement(" SELECT r.userid,r.score,u.username,u.firstname,u.lastname, r.questionjson" +
                        " FROM "+schemaname+".cyclemeetingassessmentresult r " +
                        " left join master.users u on u.id = r.userid " +
                        " WHERE isattemp = true AND userid = ? AND agendaid = ? ");
                stmt.setInt(1,userId);
                stmt.setInt(2,agendaId);

                resultSet = stmt.executeQuery();

                while (resultSet.next())
                {
                    QuestionCollection collection = new QuestionCollection();
                    collection.userId= resultSet.getInt(1);
                    collection.score = resultSet.getDouble(2);
                    collection.username = resultSet.getString(3);
                    collection.fullname = resultSet.getString(4)+ " " + resultSet.getString(5);
                    collection.questionJson =resultSet.getString(6);
                    questionsList.add(collection);
                }
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
            }
            return questionsList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}
