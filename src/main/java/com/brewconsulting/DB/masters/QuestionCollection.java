package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lcom53 on 15/12/16.
 */
public class QuestionCollection
{
    @JsonProperty("id")
    public int id;

    @JsonProperty("agendaId")
    public int agendaId;

    @JsonProperty("contentType")
    public String contentType;

    @JsonProperty("contentSeq")
    public int contentSeq;

    @JsonProperty("createBy")
    public int createBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdOn")
    public java.util.Date createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("updateOn")
    public java.util.Date updateOn;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("title")
    public String title;

    @JsonProperty("description")
    public String description;

    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonProperty("questionsId")
    public Integer[] questionsId;

    @JsonProperty("randomdelivery")
    public boolean randomdelivery;

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

    @JsonProperty("questions")
    public List<Question> questions;



    // make visible to package only
    public QuestionCollection() {
    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    public static final int Question = 15;


    /***
     *  Method is used to get all question collections.
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
            ResultSet groupResultSet = null;
            ResultSet meetingResultSet = null;
            int result;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName+".groupagenda WHERE id = ? ");
                stmt.setInt(1,node.get("agendaid").asInt());
                groupResultSet = stmt.executeQuery();

                stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName+".cyclemeetingagenda WHERE id = ? ");
                stmt.setInt(1,node.get("agendaid").asInt());
                meetingResultSet = stmt.executeQuery();

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

                if(groupResultSet.getInt("Count") > 0 && meetingResultSet.getInt("Count") ==0) {
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection"
                                            + " (agendaid,contenttype,contentseq,createdon,createdby,updateon,updatedby,"
                                            + " questions,randomdelivery,collectionseq,disregardcomplexitylevel,deliverallquestions,"
                                            + " questionbreakup,collectionname) values (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?,?)",
                                    Statement.RETURN_GENERATED_KEYS);
                }
                else{
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
     *  This method is used to get All Group or Cycle meeting Queston Collections.
     *
     * @param agendaId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<QuestionCollection> getAllQuestionCollection(int agendaId,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<QuestionCollection> collectionsList = new ArrayList<QuestionCollection>();
            QuestionCollection questionCollection;
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet questionResult = null;
            int groupCount= 0 , meetingCount=0;

            try {
                if(con != null)
                {
                    stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                            +schemaName
                            +".groupsessioncontenttestquestioncollection WHERE agendaid = ? ");
                    stmt.setInt(1,agendaId);
                    result = stmt.executeQuery();
                    while (result.next())
                    {
                        groupCount = result.getInt("Count");
                    }

                    stmt = con.prepareStatement("SELECT count(agendaid) as Count FROM "
                            +schemaName
                            +".cyclemeetingsessioncontenttestquestioncollection WHERE agendaid = ? ");
                    stmt.setInt(1,agendaId);
                    result = stmt.executeQuery();

                    while (result.next())
                    {
                        meetingCount = result.getInt("Count");
                    }

                    if(groupCount > 0 && meetingCount ==0)
                    {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby, questions, randomdelivery, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup," +
                                " collectionname, title, description " +
                                " FROM "+schemaName+".groupsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                    }
                    else
                    {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, createdon, createdby, " +
                                " updateon, updatedby,questions, randomdelivery, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup," +
                                " collectionname, title, description " +
                                " FROM "+schemaName+".cyclemeetingsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                    }

                    stmt.setInt(1,agendaId);
                    result = stmt.executeQuery();

                    while (result.next())
                    {
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

                        for(int i=0;i<questionCollection.questionsId.length;i++)
                        {
                            stmt = con.prepareStatement("SELECT id,complexitylevel,questiontype,questionjson FROM "
                                    + schemaName
                                    + ".question WHERE id = ? ");
                            stmt.setInt(1,questionCollection.questionsId[i]);
                            questionResult = stmt.executeQuery();
                            while (questionResult.next())
                            {
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
                }
                else
                    throw new Exception("DB connection is null");

            }
            finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if(questionResult != null)
                    if(!questionResult.isClosed())
                        questionResult.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return collectionsList;
        }
        else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     *  This method is used to delete Question Collections from database
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteQuestionCollections(int id,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            ResultSet resultSet = null;
            int groupCount = 0, meetingCount = 0;
            try
            {
                stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                        +".groupsessioncontenttestquestioncollection " +
                        " WHERE id = ? ");
                stmt.setInt(1,id);
                resultSet = stmt.executeQuery();
                while (resultSet.next())
                {
                    groupCount = resultSet.getInt("Count");
                }

                stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                        +".cyclemeetingsessioncontenttestquestioncollection " +
                        " WHERE id = ? ");
                stmt.setInt(1,id);
                resultSet = stmt.executeQuery();
                while (resultSet.next())
                {
                    meetingCount = resultSet.getInt("Count");
                }

                if(groupCount > 0 && meetingCount ==0)
                {
                    stmt = con.prepareStatement(" DELETE FROM "+schemaName+".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,id);
                    affectedRows = stmt.executeUpdate();
                }
                else
                {
                    stmt = con.prepareStatement(" DELETE FROM "+schemaName+".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,id);
                    affectedRows = stmt.executeUpdate();
                }
            }
            finally {
                if(resultSet != null)
                    if(!resultSet.isClosed())
                        resultSet.close();
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

    /***
     *  Method is used to remove question from question collections.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int removeQuestionFromCollection(JsonNode node,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet = null;
            int groupCount =0,meetingCount=0;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                            +".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                            +".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        meetingCount = resultSet.getInt("Count");
                    }

                    if(groupCount >0 && meetingCount ==0)
                    {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".groupsessioncontenttestquestioncollection SET questions = array_remove(questions, ? )"
                                        + " WHERE id = ?");
                    }
                    else
                    {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".cyclemeetingsessioncontenttestquestioncollection SET questions = array_remove(questions, ? )"
                                        + " WHERE id = ?");
                    }

                    stmt.setInt(1,node.get("questionId").asInt());
                    stmt.setInt(2,node.get("collectionId").asInt());

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
     * Method is used to append question in question collections.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int appendQuestionInCollection(JsonNode node,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet = null;
            int groupCount =0,meetingCount=0;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                            +".groupsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        groupCount = resultSet.getInt("Count");
                    }

                    stmt = con.prepareStatement(" SELECT count(id) as Count FROM "+schemaName
                            +".cyclemeetingsessioncontenttestquestioncollection " +
                            " WHERE id = ? ");
                    stmt.setInt(1,node.get("collectionId").asInt());
                    resultSet = stmt.executeQuery();
                    while (resultSet.next())
                    {
                        meetingCount = resultSet.getInt("Count");
                    }
                    
                    for(int i =0 ;i < node.withArray("questionId").size();i++)
                    {
                        if(groupCount >0 && meetingCount ==0)
                        {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".groupsessioncontenttestquestioncollection SET questions = array_append(questions, ? )"
                                            + " WHERE id = ?");
                        }
                        else
                        {
                            stmt = con
                                    .prepareStatement("UPDATE "
                                            + schemaName
                                            + ".cyclemeetingsessioncontenttestquestioncollection SET questions = array_append(questions, ? )"
                                            + " WHERE id = ?");
                        }

                        stmt.setInt(1,node.withArray("questionId").get(i).asInt());
                        stmt.setInt(2,node.get("collectionId").asInt());

                        result = stmt.executeUpdate();
                        result++;
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

}
