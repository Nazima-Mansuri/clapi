package com.brewconsulting.DB.masters;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.omg.CORBA.PERSIST_STORE;

import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
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

    @JsonProperty("division")
    public int division;

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

    @JsonProperty("isActive")
    public boolean isActive;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

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
    public String questionJson;

    @JsonProperty("answerJson")
    public String answerJson;

    @JsonProperty("questionImage")
    public String questionImage;

    @JsonProperty("fileType")
    public String fileType;

    // make the default constructor visible to package only.
    public Question() {
    }

    public enum QuestionType {
        MCQ, MRQ, MATRIX, SLIDER, PRIORITY, TEXT, PULLDOWN, NUMBER, LINE, DIVISION, DATE, TIME, EMAIL, URL;
    }

    public enum ComplexityLevel {
        LOW, MEDIUM, HIGH;
    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    public static final int Question = 15;

    /***
     * Method used to get all Questions
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Question> getAllQuestions(int divId, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<Question> questionsList = new ArrayList<Question>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    if (divId != -1) {
                        stmt = con
                                .prepareStatement(" SELECT q.id, questiontext, pollable, division," +
                                        " questiontype, complexitylevel, products, keywords, feedbackright, " +
                                        " feedbackwrong, q.isactive, q.createby, q.createdate, q.updatedate, " +
                                        " q.updateby, questionjson, answerjson , u.username,u.firstname,u.lastname," +
                                        " (uf.address).city city,(uf.address).state state,(uf.address).phone phone,uf.profileimage,q.imageurl,q.filetype " +
                                        " FROM " + schemaName + ".question q " +
                                        " left join master.users u on u.id = q.updateby" +
                                        " left join " + schemaName + ".userprofile uf on uf.userid = q.updateby " +
                                        " WHERE division = ? " +
                                        " ORDER BY q.createdate DESC ");
                        stmt.setInt(1, divId);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            Question question = new Question();
                            question.id = result.getInt(1);
                            question.questionText = result.getString(2);
                            question.pollable = result.getBoolean(3);
                            question.division = result.getInt(4);
                            question.questionType = result.getString(5);
                            question.complexityLevel = result.getString(6);
                            if (result.getArray(7).getArray() != null)
                                question.products = (Integer[]) result.getArray(7).getArray();
                            else
                                question.products = null;
                            question.keywords = (String[]) result.getArray(8).getArray();
                            question.feedbackRight = result.getString(9);
                            question.feedbackWrong = result.getString(10);
                            question.isActive = result.getBoolean(11);
                            question.createBy = result.getInt(12);
                            question.createDate = result.getTimestamp(13);
                            question.updatedate = result.getTimestamp(14);
                            question.updateby = result.getInt(15);
                            question.questionJson = result.getString(16);
                            question.answerJson = result.getString(17);
                            question.userDetails = new ArrayList<>();
                            question.userDetails.add(new UserDetail(result.getInt(15), result.getString(18), result.getString(19), result.getString(20), result.getString(21), result.getString(22), (String[]) result.getArray(23).getArray()));
                            question.questionImage = result.getString(25);
                            question.fileType = result.getString(26);
                            questionsList.add(question);
                        }
                    } else {
                        stmt = con
                                .prepareStatement(" SELECT q.id, questiontext, pollable, division," +
                                        " questiontype, complexitylevel, products, keywords, feedbackright, " +
                                        " feedbackwrong, q.isactive, q.createby, q.createdate, q.updatedate, " +
                                        " q.updateby, questionjson, answerjson , u.username,u.firstname,u.lastname," +
                                        " (uf.address).city city,(uf.address).state state,(uf.address).phone phone,uf.profileimage,q.imageurl,q.filetype " +
                                        " FROM " + schemaName + ".question q " +
                                        " left join master.users u on u.id = q.updateby" +
                                        " left join " + schemaName + ".userprofile uf on uf.userid = q.updateby " +
                                        " ORDER BY q.createdate DESC ");
                        result = stmt.executeQuery();
                        while (result.next()) {
                            Question question = new Question();
                            question.id = result.getInt(1);
                            question.questionText = result.getString(2);
                            question.pollable = result.getBoolean(3);
                            question.division = result.getInt(4);
                            question.questionType = result.getString(5);
                            question.complexityLevel = result.getString(6);
                            if (result.getArray(7).getArray() != null)
                                question.products = (Integer[]) result.getArray(7).getArray();
                            else
                                question.products = null;
                            question.keywords = (String[]) result.getArray(8).getArray();
                            question.feedbackRight = result.getString(9);
                            question.feedbackWrong = result.getString(10);
                            question.isActive = result.getBoolean(11);
                            question.createBy = result.getInt(12);
                            question.createDate = result.getTimestamp(13);
                            question.updatedate = result.getTimestamp(14);
                            question.updateby = result.getInt(15);
                            question.questionJson = result.getString(16);
                            question.answerJson = result.getString(17);
                            question.userDetails = new ArrayList<>();
                            question.userDetails.add(new UserDetail(result.getInt(15), result.getString(18), result.getString(19), result.getString(20), result.getString(21), result.getString(22), (String[]) result.getArray(23).getArray()));
                            question.questionImage = result.getString(25);
                            question.fileType = result.getString(26);
                            questionsList.add(question);
                        }
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
     *
     *  Method used to insert questions in database.
     *
     * @param questionText
     * @param pollable
     * @param division
     * @param questionType
     * @param complexityLevel
     * @param products
     * @param keywords
     * @param feedbackRight
     * @param feedbackWrong
     * @param isActive
     * @param questionJson
     * @param answerJson
     * @param imageUrl
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int insertQuestion(String questionText, boolean pollable, int division,
                                     String questionType, String complexityLevel, String products, String keywords, String feedbackRight, String feedbackWrong,
                                     boolean isActive, String questionJson, String answerJson, String imageUrl, String filetype, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                QuestionType quesType = QuestionType.valueOf(questionType.toUpperCase());
                ComplexityLevel level = ComplexityLevel.valueOf(complexityLevel.toUpperCase());
                Array prdarr = null;
                String[] strProductArr = products.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                Integer[] productArr = new Integer[strProductArr.length];

                if (products != null && !products.isEmpty()) {

                    for (int i = 0; i < strProductArr.length; i++) {
                        productArr[i] = Integer.parseInt(strProductArr[i]);
                    }

                    prdarr = con.createArrayOf("int", productArr);
                } else {
                    productArr = new Integer[0];
                    prdarr = con.createArrayOf("int", productArr);
                }

                Array keyArr = null;
                String[] keywordArr = keywords.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                if (keywords != null && !keywords.isEmpty()) {

                    keyArr = con.createArrayOf("text", keywordArr);
                } else {
                    keywordArr = new String[0];
                    keyArr = con.createArrayOf("text", keywordArr);
                }

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".question(questionText,pollable,division,questionType," +
                                        " complexityLevel,products,keywords,feedbackRight,feedbackWrong," +
                                        " isActive,createBy,createDate,updatedate,updateby,questionjson,answerjson,imageurl,filetype) values " +
                                        " (?,?,?," +
                                        " CAST(? AS master.questionType),CAST(? AS master.complexityLevel),?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                if (questionText != null || questionText != "")
                    stmt.setString(1, questionText);
                else
                    stmt.setString(1, null);

                stmt.setBoolean(2, pollable);

                if (division > 0)
                    stmt.setInt(3, division);
                else
                    stmt.setNull(3, 0);

                stmt.setString(4, quesType.name());
                stmt.setString(5, level.name());

                stmt.setArray(6, prdarr);
                stmt.setArray(7, keyArr);

                if (feedbackRight != null || feedbackRight != "")
                    stmt.setString(8, feedbackRight);
                else
                    stmt.setString(8, null);

                if (feedbackWrong != null || feedbackWrong != "")
                    stmt.setString(9, feedbackWrong);
                else
                    stmt.setString(9, null);

                stmt.setBoolean(10, isActive);
                stmt.setInt(11, loggedInUser.id);
                stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));
                stmt.setTimestamp(13, new Timestamp((new Date()).getTime()));
                stmt.setInt(14, loggedInUser.id);
                stmt.setString(15, String.valueOf(questionJson));
                stmt.setString(16, String.valueOf(answerJson));
                stmt.setString(17, imageUrl);
                stmt.setString(18, filetype);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Questions Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int questionId;
                if (generatedKeys.next())
                    // It gives last inserted Id in questionId
                    questionId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return questionId;


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
     *  Method is used to update question.
     *
     * @param questionText
     * @param pollable
     * @param division
     * @param questionType
     * @param complexityLevel
     * @param products
     * @param keywords
     * @param feedbackRight
     * @param feedbackWrong
     * @param isActive
     * @param questionJson
     * @param answerJson
     * @param imageUrl
     * @param questionId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateQuestion(String questionText, boolean pollable, int division,
                                     String questionType, String complexityLevel, String products, String keywords, String feedbackRight, String feedbackWrong,
                                     boolean isActive, String questionJson, String answerJson, String imageUrl, String filetype,
                                     int questionId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;

            try {
                con.setAutoCommit(false);

                QuestionType quesType = QuestionType.valueOf(questionType.toUpperCase());
                ComplexityLevel level = ComplexityLevel.valueOf(complexityLevel.toUpperCase());

                Array prdarr = null;

                String[] strProductArr = products.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                Integer[] productArr = new Integer[strProductArr.length];

                if (products != null && !products.isEmpty()) {

                    for (int i = 0; i < strProductArr.length; i++) {
                        productArr[i] = Integer.parseInt(strProductArr[i]);
                    }

                    prdarr = con.createArrayOf("int", productArr);
                } else {
                    productArr = new Integer[0];
                    prdarr = con.createArrayOf("int", productArr);
                }

                Array keyArr = null;
                String[] keywordArr = keywords.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                if (keywords != null && !keywords.isEmpty()) {

                    keyArr = con.createArrayOf("text", keywordArr);
                } else {
                    keywordArr = new String[0];
                    keyArr = con.createArrayOf("text", keywordArr);
                }

                stmt = con
                        .prepareStatement("UPDATE " + schemaName + ".question SET  " +
                                " questiontext = ?, pollable = ?, division = ?, " +
                                " complexitylevel = CAST(? AS master.complexityLevel)," +
                                " products = ?, keywords = ?, feedbackright = ?," +
                                " feedbackwrong = ?, isactive = ?,updatedate = ?, updateby = ?,  " +
                                " questionjson = ?, answerjson = ? , questiontype = CAST(? AS master.questionType), imageurl = ?, " +
                                " filetype = ? WHERE id = ?");


                if (questionText != null || questionText != "")
                    stmt.setString(1, questionText);
                else
                    stmt.setString(1, null);

                stmt.setBoolean(2, pollable);

                if (division > 0)
                    stmt.setInt(3, division);
                else
                    stmt.setNull(3, 0);

                stmt.setString(4, level.name());

                stmt.setArray(5, prdarr);
                stmt.setArray(6, keyArr);

                if (feedbackRight != null || feedbackRight != "")
                    stmt.setString(7, feedbackRight);
                else
                    stmt.setString(7, null);

                if (feedbackWrong != null || feedbackWrong != "")
                    stmt.setString(8, feedbackWrong);
                else
                    stmt.setString(8, null);

                stmt.setBoolean(9, isActive);

                stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
                stmt.setInt(11, loggedInUser.id);
                stmt.setString(12, String.valueOf(questionJson));
                stmt.setString(13, String.valueOf(answerJson));
                stmt.setString(14, quesType.name());
                stmt.setString(15, imageUrl);
                stmt.setString(16,filetype);
                stmt.setInt(17, questionId);

                affectedRows = stmt.executeUpdate();

                con.commit();
                return affectedRows;

            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }

        } else
            throw new NotAuthorizedException("");

    }


    /***
     * Method used to delete question from database.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteQuestions(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE from " + schemaName + ".question where id = ? ");
                    stmt.setInt(1, id);
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
     * Method is used to seach question from database.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Question> filterQuestions(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Question).equals("Read") ||
                Permissions.isAuthorised(userRole, Question).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<Question> questionsList = new ArrayList<Question>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            boolean isDate = false, isKeyword= false,isProduct= false,isAuthor= false;
            int index = 1;

            try {
                if (con != null) {


                    String query = "SELECT id, questiontext, pollable, division, complexitylevel, products, " +
                            " keywords, feedbackright, feedbackwrong, isactive, createby, createdate, " +
                            " updatedate, updateby, questionjson, answerjson, questiontype, " +
                            " imageurl, filetype " +
                            " FROM "+schemaName+".question WHERE division = ? ";



                    if(node.has("FromDate") && node.has("ToDate"))
                    {
                        if(node.get("FromDate").asText() != "" || node.get("ToDate").asText() != "") {
                                query = query.concat(" AND createdate >= ? AND createdate <= ( ? ::date + '1 day'::interval) ");
                                isDate = true;

                        }

                    }
                    if(node.has("Keyword"))
                    {
                        if(node.withArray("Keyword").size() > 0) {
                            query = query.concat(" AND keywords = ? ");
                            isKeyword = true;
                        }
                    }

                    if(node.has("Product"))
                    {
                        if(node.withArray("Product").size() > 0) {
                            query = query.concat(" AND products = ? ");
                            isProduct = true;
                        }
                    }

                    if(node.has("Author"))
                    {
                        if(node.get("Author").asInt() > 0) {
                            query = query.concat(" AND createby = ? ");
                            isAuthor = true;
                        }
                    }

                    stmt = con.prepareStatement(query);
                    System.out.println(" QUERY : " + query);
                    stmt.setInt(index++,node.get("divisionId").asInt());

                    if(isDate)
                    {
                        stmt.setDate(index++, java.sql.Date.valueOf(node.get("FromDate").asText()));
                        stmt.setDate(index++, java.sql.Date.valueOf(node.get("ToDate").asText()));
                    }
                    if(isKeyword)
                    {
                        String arr[] = new String[node.withArray("Keyword").size()];
                        for (int i = 0;i<node.withArray("Keyword").size();i++)
                        {
                            arr[i] = node.withArray("Keyword").get(i).asText();
                        }
                        Array array = con.createArrayOf("text",arr);
                        stmt.setArray(index++,array);
                    }

                    if(isProduct)
                    {
                        Integer arr[] = new Integer[node.withArray("Product").size()];
                        for (int i = 0;i<node.withArray("Product").size();i++)
                        {
                            arr[i] = node.withArray("Product").get(i).asInt();
                        }
                        Array array = con.createArrayOf("int",arr);
                        stmt.setArray(index++,array);
                    }
                    if(isAuthor)
                    {
                        stmt.setInt(index++,node.get("Author").asInt());
                    }

                    result = stmt.executeQuery();

                    while (result.next()) {
                        Question question = new Question();
                        question.id = result.getInt(1);
                        question.questionText = result.getString(2);
                        question.pollable = result.getBoolean(3);
                        question.division = result.getInt(4);
                        question.complexityLevel = result.getString(5);
                        if (result.getArray(7).getArray() != null)
                            question.products = (Integer[]) result.getArray(6).getArray();
                        else
                            question.products = null;
                        question.keywords = (String[]) result.getArray(7).getArray();
                        question.feedbackRight = result.getString(8);
                        question.feedbackWrong = result.getString(9);
                        question.isActive = result.getBoolean(10);
                        question.createBy = result.getInt(11);
                        question.createDate = result.getTimestamp(12);
                        question.updatedate = result.getTimestamp(13);
                        question.updateby = result.getInt(14);
                        question.questionJson = result.getString(15);
                        question.answerJson = result.getString(16);
                        question.questionType = result.getString(17);
                        question.questionImage = result.getString(18);
                        question.fileType = result.getString(19);
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
     * save uploaded file to new location
     *
     * @param inputStream
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String writeToFile(InputStream inputStream, String fileName)
            throws IOException {

        String existingBucketName = "com.brewconsulting.client1";
        String finalUrl = null;
        String amazonFileUploadLocationOriginal = existingBucketName
                + "/Question";

        try {

            AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJZZRFGQGNZIDUFTQ", "12uUP7pQrvR3Kf0GpyeJr328RQ/a1m8TI+/8w2X8");
            AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    amazonFileUploadLocationOriginal, fileName, inputStream,
                    objectMetadata);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("Etag:" + result.getETag() + "-->" + result);

            finalUrl = "https://s3.amazonaws.com/"
                    + amazonFileUploadLocationOriginal + "/" + fileName;
            System.out.println(finalUrl);

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        return finalUrl;
    }


    /***
     *  Method used to get specific Question.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Question getQuestionById(int id,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Question).equals("Read") ||
                Permissions.isAuthorised(userRole,Question).equals("Write"))
        {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            Question question = new Question();
            try
            {
                stmt = con
                        .prepareStatement(" SELECT q.id, complexitylevel, questionjson, q.imageurl,q.filetype " +
                                " FROM " + schemaName + ".question q " +
                                " WHERE id = ? " +
                                " ORDER BY q.createdate DESC ");
                        stmt.setInt(1,id);
                        result = stmt.executeQuery();

                        while (result.next())
                        {
                            question.id = result.getInt(1);
                            question.complexityLevel = result.getString(2);
                            question.questionJson = result.getString(3);
                            question.questionImage = result.getString(4);
                            question.fileType = result.getString(5);
                        }

            }
            finally {
                if(result != null)
                    if(!result.isClosed())
                        result.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
                if(con != null)
                    if(!con.isClosed())
                        con.close();
            }

            return question;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }


    /***
     *  Method is used to get Question of Given complexity level.
     *
     * @param Questions
     * @param complexitylevel
     * @return
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static List<Question> getQuestionsByListAndComplexity(Integer[] Questions , String complexitylevel) throws SQLException, NamingException, ClassNotFoundException {

        PreparedStatement stmt = null;
        List<Question> quesList = new ArrayList<>();
        Connection con = DBConnectionProvider.getConn();
        ResultSet result = null;

        try {
            for (int i = 0; i < Questions.length; i++) {

                stmt = con.prepareStatement("SELECT q.id,complexitylevel, questionjson, q.imageurl,q.filetype " +
                        " FROM client1.question q WHERE complexitylevel = CAST(? AS master.complexitylevel) " +
                        " AND id = ? ORDER BY q.createdate DESC ");
                stmt.setString(1, complexitylevel);
                stmt.setInt(2, Questions[i]);
                result = stmt.executeQuery();

                while (result.next()) {
                    Question question = new Question();
                    question.id = result.getInt(1);
                    question.complexityLevel = result.getString(2);
                    question.questionJson = result.getString(3);
                    question.questionImage = result.getString(4);
                    question.fileType = result.getString(5);
                    quesList.add(question);
                }
            }

            return quesList;
        }
        finally {
            if(result != null)
                if(!result.isClosed())
                    result.close();
            if(con != null)
                if(!con.isClosed())
                    con.close();
            if(stmt != null)
                if(!stmt.isClosed())
                    stmt.close();
        }
    }

    public static List<Question> getQuestionsByList(Integer[] Questions) throws SQLException, NamingException, ClassNotFoundException {

        PreparedStatement stmt = null;
        List<Question> quesList = new ArrayList<>();
        Connection con = DBConnectionProvider.getConn();
        ResultSet result = null;

        try {
            for (int i = 0; i < Questions.length; i++) {
                stmt = con
                        .prepareStatement(" SELECT q.id, questiontext, pollable, division," +
                                " questiontype, complexitylevel, products, keywords, feedbackright, " +
                                " feedbackwrong, q.isactive, q.createby, q.createdate, q.updatedate, " +
                                " q.updateby, questionjson, answerjson , q.imageurl,q.filetype " +
                                " FROM client1.question q " +
                                " WHERE id = ? ");
                stmt.setInt(1, Questions[i]);
                result = stmt.executeQuery();

                while (result.next()) {
                    Question question = new Question();
                    question.id = result.getInt(1);
                    question.questionText = result.getString(2);
                    question.pollable = result.getBoolean(3);
                    question.division = result.getInt(4);
                    question.questionType = result.getString(5);
                    question.complexityLevel = result.getString(6);
                    if (result.getArray(7).getArray() != null)
                        question.products = (Integer[]) result.getArray(7).getArray();
                    else
                        question.products = null;
                    question.keywords = (String[]) result.getArray(8).getArray();
                    question.feedbackRight = result.getString(9);
                    question.feedbackWrong = result.getString(10);
                    question.isActive = result.getBoolean(11);
                    question.createBy = result.getInt(12);
                    question.createDate = result.getTimestamp(13);
                    question.updatedate = result.getTimestamp(14);
                    question.updateby = result.getInt(15);
                    question.questionJson = result.getString(16);
                    question.answerJson = result.getString(17);
                    question.questionImage = result.getString(18);
                    question.fileType = result.getString(19);
                    quesList.add(question);
                }
            }

            return quesList;
        }
        finally {
            if(result != null)
                if(!result.isClosed())
                    result.close();
            if(con != null)
                if(!con.isClosed())
                    con.close();
            if(stmt != null)
                if(!stmt.isClosed())
                    stmt.close();
        }
    }
}
