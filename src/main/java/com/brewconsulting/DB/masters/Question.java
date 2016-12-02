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
 * Created by lcom53 on 7/11/16.
 */
public class Question {

    @JsonProperty("id")
    public int id;

    @JsonProperty("questionText")
    public String questionText;

    @JsonProperty("pollable")
    public boolean pollable;

    @JsonProperty("deliveryFormat")
    public String deliveryFormat;

    @JsonProperty("division")
    public int division;

    @JsonProperty("deliveryMode")
    public String deliveryMode;

    @JsonProperty("questionType")
    public String questionType;

    @JsonProperty("complexityLevel")
    public String complexityLevel;

    @JsonProperty("products")
    public Integer[] products;

    @JsonProperty("keywords")
    public String[] keywords;

    @JsonProperty("feedbackRight")
    public String feedbackRight;

    @JsonProperty("feedbackWrong")
    public String feedbackWrong;

    @JsonProperty("comments")
    public String comments;

    @JsonProperty("isActive")
    public boolean isActive;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createDate;

    @JsonProperty("updatedate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updatedate;

    @JsonProperty("updateby")
    public int updateby;

    @JsonProperty("questionJson")
    public String questionJson ;

    @JsonProperty("answerJson")
    public String answerJson;

    // make the default constructor visible to package only.
    public Question() {
    }

    public enum QuestionType
    {
        MCQ, MRQ, MATRIX,SLIDER,RANKING,TEXT,AUDIO,IMAGE,MATCH,FILL,BOOL,PULLDOWN,DRAGDROP,NUMBER;
    }

    public enum DeliveryFormat
    {
        WEB,APP, BOTH;
    }

    public enum DeliveryMode
    {
        PILL,CYCLE,BOTH;
    }

    public enum ComplexityLevel
    {
        LOW,MEDIUM,HIGH;
    }

    public static final int Question = 15;

    /***
     *  Method used to get all Questions
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Question> getAllQuestions(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;
        System.out.println("User Role : " + userRole);

        if (Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<Question> questionsList = new ArrayList<Question>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT id, questiontext, pollable, deliveryformat, division, deliverymode," +
                                    " questiontype, complexitylevel, products, keywords, feedbackright, " +
                                    " feedbackwrong, comments, isactive, createby, createdate, updatedate, " +
                                    " updateby, questionjson, answerjson " +
                                    " FROM "+schemaName+".question ");
                    result = stmt.executeQuery();
                    System.out.print(result);
                    while (result.next()) {
                        Question question = new Question();
                        question.id = result.getInt(1);
                        question.questionText = result.getString(2);
                        question.pollable = result.getBoolean(3);
                        question.deliveryFormat = result.getString(4);
                        question.division = result.getInt(5);
                        question.deliveryMode = result.getString(6);
                        question.questionType = result.getString(7);
                        question.complexityLevel = result.getString(8);
                        question.products = (Integer[]) result.getArray(9).getArray();
                        question.keywords = (String[]) result.getArray(10).getArray();
                        question.feedbackRight = result.getString(11);
                        question.feedbackWrong = result.getString(12);
                        question.comments = result.getString(13);
                        question.isActive = result.getBoolean(14);
                        question.createBy = result.getInt(15);
                        question.createDate = result.getTimestamp(16);
                        question.updatedate = result.getTimestamp(17);
                        question.updateby = result.getInt(18);
                        question.questionJson = result.getString(19);
                        System.out.println("Question : " + result.getString(19));
                        question.answerJson = result.getString(20);
                        System.out.println("Answer : " + result.getString(20));

                        questionsList.add(question);
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return questionsList;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     *  Method used to insert qustion in database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addQuestion(JsonNode node,LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try
            {
                con.setAutoCommit(false);

                DeliveryFormat format = DeliveryFormat.valueOf(node.get("deliveryFormat").asText());
                DeliveryMode mode = DeliveryMode.valueOf(node.get("deliveryMode").asText());
                QuestionType quesType = QuestionType.valueOf(node.get("questionType").asText());
                ComplexityLevel level = ComplexityLevel.valueOf(node.get("complexityLevel").asText());

                Integer[] productArr = new Integer[node.withArray("Product").size()];
                String[] keyArr = new String[node.withArray("keywords").size()];

                // Convert JsonArray into Integer Array for Products
                for (int i = 0; i < node.withArray("Product").size(); i++) {
                    productArr[i] = node.withArray("Product").get(i).asInt();
                }

                // Convert JsonArray into String Array for Keywords
                for (int i = 0; i < node.withArray("keywords").size(); i++) {
                    if(node.withArray("keywords").get(i).asText() != null ||
                            node.withArray("keywords").get(i).asText() != "") {
                        keyArr[i] = node.withArray("keywords").get(i).asText();
                    }
                }

                Array product = con.createArrayOf("int", productArr);
                Array keyword = con.createArrayOf("text",keyArr);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".question(questionText,pollable,deliveryFormat,division,deliveryMode,questionType," +
                                        " complexityLevel,products,keywords,feedbackRight,feedbackWrong,comments," +
                                        " isActive,createBy,createDate,updatedate,updateby,questionjson,answerjson) values " +
                                        " (?,?,CAST(? AS master.deliveryFormat),?,CAST(? AS master.deliveryMode)," +
                                        " CAST(? AS master.questionType),CAST(? AS master.complexityLevel),?,?,?,?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, node.get("questionText").asText());
                stmt.setBoolean(2,node.get("pollable").asBoolean());
                stmt.setString(3,format.name());
                stmt.setInt(4,node.get("division").asInt());
                stmt.setString(5,mode.name());
                stmt.setString(6,quesType.name());
                stmt.setString(7,level.name());
                stmt.setArray(8,product);
                stmt.setArray(9,keyword);
                stmt.setString(10,node.get("feedbackRight").asText());
                stmt.setString(11,node.get("feedbackWrong").asText());
                stmt.setString(12,node.get("comment").asText());
                stmt.setBoolean(13,node.get("isActive").asBoolean());
                stmt.setInt(14,loggedInUser.id);
                stmt.setTimestamp(15, new Timestamp((new Date()).getTime()));
                stmt.setTimestamp(16, new Timestamp((new Date()).getTime()));
                stmt.setInt(17,loggedInUser.id);
                stmt.setString(18, String.valueOf(node.get("questionJson")));
                stmt.setString(19, String.valueOf(node.get("correctAnswer")));

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Questions Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int questionId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    questionId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");
                System.out.println("ID : " + questionId);

                con.commit();
                return questionId;
            }
            catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        }
        else
        {
            throw new NotAuthorizedException("");
        }

    }

    /***
     *  Method used to delete question from database.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteQuestions(int id,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if(Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            try {
                if(con != null)
                {
                    stmt = con.prepareStatement("DELETE from "+schemaName+".question where id = ? ");
                    stmt.setInt(1,id);
                    affectedRows = stmt.executeUpdate();
                }
                else
                    throw new Exception("DB connection is null");

            }finally {
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
