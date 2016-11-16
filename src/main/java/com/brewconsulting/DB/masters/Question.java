package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

import static com.brewconsulting.DB.utils.stringToDate;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createDate;

    @JsonProperty("updatedate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updatedate;

    @JsonProperty("updateby")
    public int updateby;

    @JsonProperty("options")
    public String[] options;

    @JsonProperty("correctOption")
    public int correctOption;

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

    /***
     *  Method used to insert Questions and MCQQuestions in database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addMCQQurstion(JsonNode node,LoggedInUser loggedInUser) throws Exception {

        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int result = 0;
        int correctoption = 0;

        try
        {
            con.setAutoCommit(false);

            DeliveryFormat format = DeliveryFormat.valueOf(node.get("deliveryFormat").asText());
            DeliveryMode mode = DeliveryMode.valueOf(node.get("deliveryMode").asText());
            QuestionType quesType = QuestionType.valueOf(node.get("questionType").asText());
            ComplexityLevel level = ComplexityLevel.valueOf(node.get("complexityLevel").asText());

            Integer[] productArr = new Integer[node.withArray("products").size()];
            String[] keyArr = new String[node.withArray("keywords").size()];
            String[] oprionArr = new String[node.withArray("options").size()];

            // Convert JsonArray into Integer Array for Products
            for (int i = 0; i < node.withArray("products").size(); i++) {
                productArr[i] = node.withArray("products").get(i).asInt();
            }

            // Convert JsonArray into String Array for Keywords
            for (int i = 0; i < node.withArray("keywords").size(); i++) {
                keyArr[i] = node.withArray("keywords").get(i).asText();
            }

            // Convert JsonArray into String Array for Options
            for (int i = 0; i < node.withArray("options").size(); i++) {
                oprionArr[i] = node.withArray("options").get(i).asText();

                if(oprionArr[i].equals(node.get("correctOption").asText()))
                {
                    correctoption = i;
                }
            }

            Array product = con.createArrayOf("int", productArr);
            Array keyword = con.createArrayOf("text",keyArr);
            Array option = con.createArrayOf("text",oprionArr);

            stmt = con
                    .prepareStatement(
                            "INSERT INTO "
                                    + schemaName
                                    + ".mcqquestions(questionText,pollable,deliveryFormat,division,deliveryMode,questionType," +
                                    " complexityLevel,products,keywords,feedbackRight,feedbackWrong,comments," +
                                    " isActive,createBy,createDate,updatedate,updateby,options,correctOption) values " +
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
            stmt.setString(12,node.get("comments").asText());
            stmt.setBoolean(13,node.get("isActive").asBoolean());
            stmt.setInt(14,loggedInUser.id);
            stmt.setTimestamp(15, new Timestamp((new Date()).getTime()));
            stmt.setTimestamp(16, new Timestamp((new Date()).getTime()));
            stmt.setInt(17,loggedInUser.id);
            stmt.setArray(18,option);
            stmt.setInt(19,correctoption);

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

    public static int addQuestion(JsonNode node,LoggedInUser loggedInUser) throws Exception {

        String schemaName = loggedInUser.schemaName;
        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        int result = 0;
        int correctoption = 0;

        try
        {
            con.setAutoCommit(false);

            DeliveryFormat format = DeliveryFormat.valueOf(node.get("deliveryFormat").asText());
            DeliveryMode mode = DeliveryMode.valueOf(node.get("deliveryMode").asText());
            QuestionType quesType = QuestionType.valueOf(node.get("questionType").asText());
            ComplexityLevel level = ComplexityLevel.valueOf(node.get("complexityLevel").asText());

            Integer[] productArr = new Integer[node.withArray("products").size()];
            String[] keyArr = new String[node.withArray("keywords").size()];

            // Convert JsonArray into Integer Array for Products
            for (int i = 0; i < node.withArray("products").size(); i++) {
                productArr[i] = node.withArray("products").get(i).asInt();
            }

            // Convert JsonArray into String Array for Keywords
            for (int i = 0; i < node.withArray("keywords").size(); i++) {
                keyArr[i] = node.withArray("keywords").get(i).asText();
            }

            Array product = con.createArrayOf("int", productArr);
            Array keyword = con.createArrayOf("text",keyArr);

            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(node.get("questionJson").asText().toString());

            PGobject jsonObject1 = new PGobject();
            jsonObject1.setType("json");
            jsonObject1.setValue(node.get("correctAnswer").asText().toString());

            stmt = con
                    .prepareStatement(
                            "INSERT INTO "
                                    + schemaName
                                    + ".question(questionText,pollable,deliveryFormat,division,deliveryMode,questionType," +
                                    " complexityLevel,products,keywords,feedbackRight,feedbackWrong,comments," +
                                    " isActive,createBy,createDate,updatedate,updateby,questionjson,answerjson) values " +
                                    " (?,?,CAST(? AS master.deliveryFormat),?,CAST(? AS master.deliveryMode)," +
                                    " CAST(? AS master.questionType),CAST(? AS master.complexityLevel),?,?,?,?,?,?,?,?,?,?,to_json(?::json),to_json(?::json))",
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
            stmt.setString(12,node.get("comments").asText());
            stmt.setBoolean(13,node.get("isActive").asBoolean());
            stmt.setInt(14,loggedInUser.id);
            stmt.setTimestamp(15, new Timestamp((new Date()).getTime()));
            stmt.setTimestamp(16, new Timestamp((new Date()).getTime()));
            stmt.setInt(17,loggedInUser.id);
            stmt.setString(18,node.get("questionJson").asText().toString());
            stmt.setString(19,node.get("correctAnswer").asText().toString());

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
}
