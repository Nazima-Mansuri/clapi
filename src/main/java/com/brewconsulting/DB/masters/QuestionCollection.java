package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.events.Event;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;
import org.joda.time.Interval;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarOutputStream;

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

    @JsonView({UserViews.collectionView.class})
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

    @JsonView({UserViews.settingView.class,UserViews.quesSetView.class})
    @JsonProperty("Instruction")
    public String Instrction;

    @JsonView({UserViews.settingView.class,UserViews.quesSetView.class})
    @JsonProperty("EndNote")
    public String EndNote;

    @JsonView({UserViews.settingView.class,UserViews.quesSetView.class})
    @JsonProperty("Description")
    public String Description;

    @JsonView({UserViews.settingView.class})
    @JsonProperty("showFeedBack")
    public boolean showFeedBack;

    @JsonView({UserViews.settingView.class,UserViews.quesSetView.class})
    @JsonProperty("AllowReview")
    public boolean AllowReview;

    @JsonView({UserViews.settingView.class})
    @JsonProperty("Scoring")
    public HashMap Scoring;

    @JsonView({UserViews.settingView.class,UserViews.quesSetView.class})
    @JsonProperty("TimeLimitation")
    public HashMap TimeLimitation;

    @JsonView({UserViews.collectionView.class,UserViews.quesSetView.class})
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

    // make visible to package only
    public QuestionCollection() {
    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
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
            int groupCount = 0, meetingCount = 0;
            int result;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName + ".groupagenda WHERE id = ? ");
                stmt.setInt(1, node.get("agendaid").asInt());
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    groupCount = resultSet.getInt(1);
                }


                stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName + ".cyclemeetingagenda WHERE id = ? ");
                stmt.setInt(1, node.get("agendaid").asInt());
                resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    meetingCount = resultSet.getInt(1);
                }

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

                if (groupCount > 0 && meetingCount == 0) {
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection"
                                            + " (agendaid,contenttype,contentseq,createdon,createdby,updateon,updatedby,"
                                            + " questions,randomdelivery,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                            + " questionbreakup,collectionname) values (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);
                } else {
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection"
                                            + " (agendaid,contenttype,contentseq,createdon,createdby,updateon,updatedby,"
                                            + " questions,randomdelivery,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                            + " questionbreakup,collectionname) values (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);
                }

                stmt.setInt(1, node.get("agendaid").asInt());
                stmt.setString(2, typeContent.name());
                stmt.setInt(3, 1);
                stmt.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(5, loggedInUser.id);
                stmt.setTimestamp(6, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(7, loggedInUser.id);
                stmt.setArray(8, question);
                stmt.setBoolean(9, node.get("randomdelivery").asBoolean());
                stmt.setInt(10, 1);
                stmt.setBoolean(11, node.get("disregardcomplexitylevel").asBoolean());
                stmt.setBoolean(12, node.get("deliverallquestions").asBoolean());
                stmt.setArray(13, quesBreak);
                stmt.setString(14, node.get("collectionname").asText());
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
    public static List<QuestionCollection> getAllQuestionCollection(int agendaId, LoggedInUser loggedInUser) throws Exception {
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
            int groupCount = 0, meetingCount = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                            + schemaName
                            + ".groupsessioncontenttestquestioncollection WHERE agendaid = ? ");
                    stmt.setInt(1, agendaId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        groupCount = result.getInt("Count");
                    }

                    stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                            + schemaName
                            + ".cyclemeetingsessioncontenttestquestioncollection WHERE agendaid = ? ");
                    stmt.setInt(1, agendaId);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        meetingCount = result.getInt("Count");
                    }

                    if (groupCount > 0 && meetingCount == 0) {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby, questions, randomdelivery, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup," +
                                " collectionname, title, description " +
                                " FROM " + schemaName + ".groupsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                    } else {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby,questions, randomdelivery, collectionseq, " +
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
                        questionCollection.randomdelivery = result.getBoolean(10);
                        questionCollection.collectionseq = result.getInt(11);
                        questionCollection.disregardcomplexitylevel = result.getBoolean(12);
                        questionCollection.deliverallquestions = result.getBoolean(13);
                        questionCollection.questionbreakup = (Integer[]) result.getArray(14).getArray();
                        questionCollection.collection = result.getString(15);
                        questionCollection.title = result.getString(16);
                        questionCollection.description = result.getString(17);
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
    public static int deleteQuestionCollections(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            ResultSet resultSet = null;
            int groupCount = 0, meetingCount = 0;
            try {
                stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                        + ".groupsessioncontenttestquestioncollection " +
                        " WHERE id = ? ");
                stmt.setInt(1, id);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    groupCount = resultSet.getInt("Count");
                }

                stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                        + ".cyclemeetingsessioncontenttestquestioncollection " +
                        " WHERE id = ? ");
                stmt.setInt(1, id);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    meetingCount = resultSet.getInt("Count");
                }

                if (groupCount > 0 && meetingCount == 0) {
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
            int groupCount = 0, meetingCount = 0;
            Integer[] array = new Integer[3];

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        meetingCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(complexitylevel) FROM " + schemaName +
                            ".question WHERE complexitylevel = ? AND id = ? ");

                    if (groupCount > 0 && meetingCount == 0) {
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
            int groupCount = 0, meetingCount = 0, gCount = 0, mCount = 0;
            List<Integer> list = new ArrayList<>();

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        meetingCount = resultSet.getInt("Count");
                    }

                    for (int i = 0; i < node.withArray("questionId").size(); i++) {
                        if (groupCount > 0 && meetingCount == 0) {
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
            int groupCount = 0, meetingCount = 0;
            int affectedRow;
            Array quesBreak = null;

            try {
                if (con != null) {
                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM " + schemaName
                            + ".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        meetingCount = resultSet.getInt("Count");
                    }

                    if (groupCount > 0 && meetingCount == 0) {
                        if (node.get("deliverallquestions").asBoolean()) {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, randomdelivery = ?, deliverallquestions = ? "
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
                                            + " SET collectionname = ?, randomdelivery = ?, deliverallquestions = ?,"
                                            + " questionbreakup = ?,disregardcomplexitylevel = ?"
                                            + " WHERE id = ? ");
                        }
                        stmt.setString(1, node.get("collectionname").asText());
                        stmt.setBoolean(2, node.get("randomdelivery").asBoolean());
                        stmt.setBoolean(3, node.get("deliverallquestions").asBoolean());

                        if (!node.get("deliverallquestions").asBoolean()) {
                            stmt.setArray(4, quesBreak);
                            stmt.setBoolean(5, node.get("disregardcomplexitylevel").asBoolean());
                            stmt.setInt(6, node.get("collectionId").asInt());
                        } else
                            stmt.setInt(4, node.get("collectionId").asInt());

                        affectedRow = stmt.executeUpdate();
                    } else {
                        if (node.get("deliverallquestions").asBoolean()) {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection "
                                            + " SET collectionname = ?, randomdelivery = ?, deliverallquestions = ? "
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
                                            + " SET collectionname = ?, randomdelivery = ?, deliverallquestions = ?,"
                                            + " questionbreakup = ?,disregardcomplexitylevel = ?"
                                            + " WHERE id = ? ");
                        }
                        stmt.setString(1, node.get("collectionname").asText());
                        stmt.setBoolean(2, node.get("randomdelivery").asBoolean());
                        stmt.setBoolean(3, node.get("deliverallquestions").asBoolean());

                        if (!node.get("deliverallquestions").asBoolean()) {
                            stmt.setArray(4, quesBreak);
                            stmt.setBoolean(5, node.get("disregardcomplexitylevel").asBoolean());
                            stmt.setInt(6, node.get("collectionId").asInt());
                        } else
                            stmt.setInt(4, node.get("collectionId").asInt());

                        affectedRow = stmt.executeUpdate();
                    }

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
            int groupCount = 0, meetingCount = 0;
            int affectedRow = 0;
            try {

                if (con != null) {
                    stmt = con.prepareStatement(" SELECT count(agendaid) as Count FROM " + schemaName
                            + ".groupsessioncontenttest " +
                            " WHERE agendaid = ? ");
                    stmt.setInt(1, node.get("agendaid").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(agendaid) as Count FROM " + schemaName
                            + ".cyclemeetingsessioncontenttest " +
                            " WHERE agendaid = ? ");
                    stmt.setInt(1, node.get("agendaid").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        meetingCount = resultSet.getInt("Count");
                    }

                    Array correctScoreArray = null;
                    Array inCorrectScoreArray = null;
                    Array diffTimeArray = null;

                    if (groupCount > 0 && meetingCount == 0) {
                        stmt = con.prepareStatement(" UPDATE "
                                + schemaName
                                + ".groupsessioncontenttest SET testinstruction = ?, testendnote = ?,testdescription=?,"
                                + " showfeedback = ?, allowreview = ?, applyscoring = ?, scorecorrect = ?,scoreincorrect = ?,"
                                + " applytimeperquestion = ?, timeperquestion= ?, duration = CAST (? AS INTERVAL) "
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


                        stmt.setInt(12, node.get("agendaid").asInt());

                        affectedRow = stmt.executeUpdate();

                    } else {

                        stmt = con.prepareStatement(" UPDATE "
                                + schemaName
                                + ".cyclemeetingsessioncontenttest SET testinstruction = ?, testendnote = ?,testdescription=?,"
                                + " showfeedback = ?, allowreview = ?, applyscoring = ?, scorecorrect = ?,scoreincorrect = ?,"
                                + " applytimeperquestion = ?, timeperquestion= ?, duration = CAST (? AS INTERVAL) "
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


                        stmt.setInt(12, node.get("agendaid").asInt());

                        affectedRow = stmt.executeUpdate();

                    }

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
    public static List<QuestionCollection> getSettingDetails(int agendaId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int groupCount = 0, meetingCount = 0;
            int affectedRow = 0;
            List<QuestionCollection> collectionList = new ArrayList<>();
            QuestionCollection questionCollection;
            try {
                stmt = con.prepareStatement(" SELECT count(agendaid) as Count FROM " + schemaName
                        + ".groupsessioncontenttest " +
                        " WHERE agendaid = ? ");
                stmt.setInt(1, agendaId);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    groupCount = resultSet.getInt("Count");
                }

                stmt = con.prepareStatement(" SELECT count(agendaid) as Count FROM " + schemaName
                        + ".cyclemeetingsessioncontenttest " +
                        " WHERE agendaid = ? ");
                stmt.setInt(1, agendaId);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    meetingCount = resultSet.getInt("Count");
                }

                if (groupCount > 0 && meetingCount == 0) {
                    stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                            " updateon, updatedby, testinstruction, testendnote,testdescription, applyscoring, " +
                            " scorecorrect, scoreincorrect, showfeedback, duration, " +
                            " timeperquestion, applytimeperquestion, allowreview, title, description " +
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

                        collectionList.add(questionCollection);
                    }
                } else {
                    stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                            " updateon, updatedby, testinstruction, testendnote,testdescription, applyscoring, " +
                            " scorecorrect, scoreincorrect, showfeedback, duration, " +
                            " timeperquestion, applytimeperquestion, allowreview, title, description " +
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
    public static List<QuestionCollection> getQuesionSet(int agendaId, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            PreparedStatement stmt = null;
            Connection con = DBConnectionProvider.getConn();
            ResultSet resultSet = null;
            List<QuestionCollection> lstCollection = new ArrayList<>();
            lstCollection = getAllQuestionCollection(agendaId, loggedInUser);

            List<QuestionCollection> questionCollectionList = new ArrayList<>();
            boolean isValid = true;
            int groupCount = 0,meetingCount = 0;

            try
            {
                if(con != null)
                {
                    if (isValid) {

                        stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                                + schemaName
                                + ".groupsessioncontenttestquestioncollection WHERE agendaid = ? ");
                        stmt.setInt(1, agendaId);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            groupCount = resultSet.getInt("Count");
                        }

                        stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                                + schemaName
                                + ".cyclemeetingsessioncontenttestquestioncollection WHERE agendaid = ? ");
                        stmt.setInt(1, agendaId);
                        resultSet = stmt.executeQuery();

                        while (resultSet.next()) {
                            meetingCount = resultSet.getInt("Count");
                        }

                        if(groupCount >0 && meetingCount == 0) {
                            stmt = con.prepareStatement(" SELECT sessionname, testinstruction, testendnote, testdescription," +
                                    " allowreview, timeperquestion,applytimeperquestion, duration " +
                                    " FROM " + schemaName + ".groupagenda g " +
                                    " left join " + schemaName + ".groupsessioncontenttest t on t.agendaid = g.id " +
                                    " WHERE t.agendaid = ? ");
                        }
                        else
                        {
                            stmt = con.prepareStatement(" SELECT sessionname, testinstruction, testendnote, testdescription," +
                                    " allowreview, timeperquestion,applytimeperquestion, duration " +
                                    " FROM " + schemaName + ".cyclemeetingagenda g " +
                                    " left join " + schemaName + ".cyclemeetingsessioncontenttest t on t.agendaid = g.id " +
                                    " WHERE t.agendaid = ? ");
                        }
                            stmt.setInt(1,agendaId);
                            resultSet = stmt.executeQuery();
                            while (resultSet.next())
                            {
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
                    }
                    else
                    {

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
            return questionCollectionList;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }
}
