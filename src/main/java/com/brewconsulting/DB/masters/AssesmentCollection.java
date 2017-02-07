package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lcom62_one on 2/3/2017.
 */
public class AssesmentCollection {

    @JsonProperty("id")
    public int id;

    @JsonProperty("testId")
    public int testId;

    @JsonProperty("questionsId")
    public Integer[] questionsId;

    @JsonProperty("collectionseq")
    public int collectionseq;

    @JsonProperty("disregardcomplexitylevel")
    public boolean disregardcomplexitylevel;

    @JsonProperty("deliverallquestions")
    public boolean deliverallquestions;

    @JsonProperty("questionbreakup")
    public Integer[] questionbreakup;

    @JsonProperty("collection")
    public String collection;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createOn")
    public java.util.Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("questions")
    public List<Question> questions;


    public AssesmentCollection() {
    }

    /***
     *  Method used to get all Assesment Collection.
     *
     * @param testId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<AssesmentCollection> getAllAssesmentCollections(int testId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Read") ||
                Permissions.isAuthorised(userRole, 20).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<AssesmentCollection> collectionsList = new ArrayList<AssesmentCollection>();
            AssesmentCollection assesmentCollection;
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet questionResult = null;

            try {
                if (con != null) {


                    stmt = con.prepareStatement("SELECT id, testid, questions, collectionseq, disregardcomplexitylevel," +
                            " deliverallquestions,questionbreakup, collectionname, createon, createby " +
                            " FROM " + schemaName + ".onthegocontenttestquestioncollection " +
                            " WHERE testid = ? ");

                    stmt.setInt(1, testId);
                    result = stmt.executeQuery();

                    while (result.next()) {
                        assesmentCollection = new AssesmentCollection();

                        assesmentCollection.id = result.getInt(1);
                        assesmentCollection.testId = result.getInt(2);
                        assesmentCollection.questionsId = (Integer[]) result.getArray(3).getArray();
                        assesmentCollection.collectionseq = result.getInt(4);
                        assesmentCollection.disregardcomplexitylevel = result.getBoolean(5);
                        assesmentCollection.deliverallquestions = result.getBoolean(6);
                        assesmentCollection.questionbreakup = (Integer[]) result.getArray(7).getArray();
                        assesmentCollection.collection = result.getString(8);
                        assesmentCollection.createOn = result.getTimestamp(9);
                        assesmentCollection.createBy = result.getInt(10);

                        assesmentCollection.questions = new ArrayList<>();

                        for (int i = 0; i < assesmentCollection.questionsId.length; i++) {
                            stmt = con.prepareStatement("SELECT id,complexitylevel,questiontype,questionjson FROM "
                                    + schemaName
                                    + ".question WHERE id = ? ");
                            stmt.setInt(1, assesmentCollection.questionsId[i]);
                            questionResult = stmt.executeQuery();
                            while (questionResult.next()) {
                                com.brewconsulting.DB.masters.Question question = new Question();
                                question.id = questionResult.getInt(1);
                                question.complexityLevel = questionResult.getString(2);
                                question.questionType = questionResult.getString(3);
                                question.questionJson = questionResult.getString(4);
                                assesmentCollection.questions.add(question);
                            }
                        }
                        collectionsList.add(assesmentCollection);
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
     *  Method is used to add Assesment Question collection.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addAssesmentQuesCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {

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

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".onthegocontenttestquestioncollection"
                                        + " (testid,questions,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                        + " questionbreakup,collectionname,createon,createby) "
                                        + " values (?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("testId").asInt());
                stmt.setArray(2, question);
                stmt.setInt(3, 1);
                stmt.setBoolean(4, node.get("disregardcomplexitylevel").asBoolean());
                stmt.setBoolean(5, node.get("deliverallquestions").asBoolean());
                stmt.setArray(6, quesBreak);
                stmt.setString(7, node.get("collectionname").asText());
                stmt.setTimestamp(8, new Timestamp((new java.util.Date()).getTime()));
                stmt.setInt(9, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Assesment Question Collection Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int quesCollectionId;
                if (generatedKeys.next())
                    // It gives last inserted Id in quesCollectionId
                    quesCollectionId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();

                arrangeAssesmentQuestions(node.get("testId").asInt(),loggedInUser);
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
     * Method is used to append new question in aessement question collection
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int appendQuesInAssesmentCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {

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

                        stmt = con.prepareStatement(" SELECT count(*) FROM " +
                                schemaName +
                                ".onthegocontenttestquestioncollection " +
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
                                            + ".onthegocontenttestquestioncollection SET questions = array_append(questions, ? )"
                                            + " WHERE id = ?");

                            stmt.setInt(1, node.withArray("questionId").get(i).asInt());
                            stmt.setInt(2, node.get("collectionId").asInt());

                            result = stmt.executeUpdate();
                            list.add(result);
                        } else {
                            result = result + 0;
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
     *  Method is used to remove question from Existing Assesment Question Collection.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeQuesFromAssesmentCollection(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet = null;
            Integer[] array = new Integer[3];

            try {
                if (con != null) {

                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".onthegocontenttestquestioncollection SET questions = array_remove(questions, ? )"
                                    + " WHERE id = ?");
                    stmt.setInt(1, node.get("questionId").asInt());
                    stmt.setInt(2, node.get("collectionId").asInt());

                    result = stmt.executeUpdate();

                    stmt = con.prepareStatement("SELECT questionbreakup FROM "
                            + schemaName
                            + ".onthegocontenttestquestioncollection WHERE id = ? ");
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
                                ".onthegocontenttestquestioncollection SET questionbreakup = ? " +
                                " WHERE id = ? ");
                        stmt.setArray(1, arr);
                        stmt.setInt(2, node.get("collectionId").asInt());
                        stmt.executeUpdate();
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
     *  Method is used to delete Assesment Question collection.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteAssesmentCollections(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            ResultSet resultSet = null;

            try {
                    stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".onthegocontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1, id);
                    affectedRows = stmt.executeUpdate();

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
     *  Method is used to update Assesment collection details.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateAssesmentCollections(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            int affectedRow;
            Array quesBreak = null;

            try {
                if (con != null) {

                        if (node.get("deliverallquestions").asBoolean()) {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".onthegocontenttestquestioncollection "
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
                                            + ".onthegocontenttestquestioncollection "
                                            + " SET collectionname = ?, deliverallquestions = ?, "
                                            + " questionbreakup = ?,disregardcomplexitylevel = ? "
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

                    arrangeAssesmentQuestions(node.get("testId").asInt(),loggedInUser);
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
     *
     *
     * @param testId
     * @param loggedInUser
     * @throws Exception
     */
    public static void arrangeAssesmentQuestions(int testId, LoggedInUser loggedInUser) throws Exception {
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> questionIds = new ArrayList<>();
        boolean isRandom = false;
        try {
            String schemaName = loggedInUser.schemaName;
            List<AssesmentCollection> lstCollection;
            lstCollection = getAllAssesmentCollections(testId, loggedInUser);

            System.out.println(" List Size : " + lstCollection.size());

            if (lstCollection != null && lstCollection.size() > 0) {
                for (AssesmentCollection qc : lstCollection) {
                    if (qc.deliverallquestions) {
                        for (int index = 0; index < qc.questionsId.length; index++) {
                            questionIds.add(qc.questionsId[index]);
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

                stmt = con.prepareStatement("SELECT id,questionids FROM " + schemaName + ".onthegoassessmentactual" +
                        " WHERE testid = ? ");
                stmt.setInt(1, testId);
                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    System.out.println(" In IF ...");
                    stmt = con.prepareStatement(" INSERT  INTO " + schemaName
                            + ".onthegoassessmentactual( testid, questionids,israndom)" +
                            " VALUES (?, ?, ?)");
                    stmt.setInt(1,testId );
                    stmt.setArray(2, quesArr);
                    stmt.setBoolean(3, isRandom);
                    stmt.executeUpdate();
                } else {
                    stmt = con.prepareStatement(" UPDATE " + schemaName + ".onthegoassessmentactual" +
                            " SET questionids = ?, israndom = ? " +
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
     * Method is used to add Assesment Answer.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addAssesmentAnswer(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, 20).equals("Write")) {
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
                            + ".onthegocontenttest WHERE testid = ? ");
                    stmt.setInt(1, node.get("testId").asInt());
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


                        stmt = con.prepareStatement(" SELECT count(*) AS Count FROM "+schemaname+".onthegoassessmentactualresult" +
                                " WHERE testid = ? AND userid = ? AND questionid = ? ");
                        stmt.setInt(1,node.get("testId").asInt());
                        stmt.setInt(2,node.get("userId").asInt());
                        stmt.setInt(3,node.get("questionId").asInt());
                        updateResultSet = stmt.executeQuery();
                        while (updateResultSet.next())
                        {
                            if(updateResultSet.getInt("Count") > 0)
                            {
                                stmt = con.prepareStatement(" UPDATE "+schemaname+".onthegoassessmentactualresult " +
                                        " SET answerjson = ?, questionjson = ?, useranswerjson = ?, score = ?,isattemp = ? ,isreview = ?" +
                                        " WHERE testid = ? AND userid = ? AND questionid = ?");
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

                                stmt.setInt(7,node.get("testId").asInt());
                                stmt.setInt(8,node.get("userId").asInt());
                                stmt.setInt(9,node.get("questionId").asInt());
                            }
                            else
                            {
                                stmt = con.prepareStatement(" INSERT INTO " + schemaname
                                        + ".onthegoassessmentactualresult(questionid, userid, answerjson, questionjson, " +
                                        " useranswerjson, score,testid,isattemp,isreview)" +
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

                                stmt.setInt(7, node.get("testId").asInt());
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
}
