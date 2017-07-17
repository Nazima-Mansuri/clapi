package com.brewconsulting.DB.masters;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.imageio.ImageIO;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by lcom53 on 29/12/16.
 */
public class Pill {

    @JsonProperty("id")
    public int id;

    @JsonProperty("divid")
    public int divid;

    @JsonProperty("divname")
    public String divName;

    @JsonProperty("title")
    public String title;

    @JsonProperty("body")
    public String body;

    @JsonProperty("questiontype")
    public String questiontype;

    @JsonProperty("isQuestion")
    public boolean isQuestion;

    @JsonProperty("products")
    public Integer[] products;

    @JsonProperty("keywords")
    public String[] keywords;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createdate")
    public Date createdate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonProperty("createby")
    public int createby;

    @JsonProperty("isImage")
    public boolean isImage;

    @JsonProperty("isVideo")
    public boolean isVideo;

    @JsonProperty("pillImages")
    public ArrayList<String> pillImages;

    @JsonProperty("pillResizeImages")
    public ArrayList<String> pillResizeImages;

    @JsonProperty("pillVideo")
    public String pillVideo;

    @JsonProperty("feedDeliveryId")
    public int feedDeliveryId;

    @JsonProperty("deliveryTime")
    public String deliveryTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonProperty("readTime")
    public Date readTime;

    @JsonProperty("userAnswerJson")
    public String userAnswerJson;

    @JsonProperty("question")
    public HashMap question;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;


    // make the default constructor visible to package only.
    public Pill() {
    }

    public enum QuestionType {
        MCQ, MRQ, MATRIX, SLIDER, PRIORITY, TEXT, PULLDOWN, NUMBER, LINE, DIVISION, DATE, TIME, EMAIL, URL, STAR, NONE;
    }

    public static final int Pill = 18;

    /***
     * Used to get all delivered pills of specific user
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> getAllPills(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Pill> pillList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet result = null;
            ResultSet resultSet = null;
            ResultSet contentSet = null;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT id,pillid,deliverytime,readtime,answerjson FROM " + schemaName + ".feeddelivery WHERE userid = ?");
                    stmt.setInt(1, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Pill pill = new Pill();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                        String date = sdf.format(result.getTimestamp(3));
                        pill.feedDeliveryId = result.getInt(1);
                        pill.deliveryTime = date;

                        pill.readTime = result.getTimestamp(4);
                        pill.userAnswerJson = result.getString(5);

                        stmt = con.prepareStatement(" SELECT id, divid, title, body, questiontype, questiontext, answeroptions, " +
                                " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby " +
                                " FROM " + schemaName + ".pills p " +
                                " WHERE id = ? ");
                        stmt.setInt(1, result.getInt(2));
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {

                            pill.id = resultSet.getInt(1);
                            pill.divid = resultSet.getInt(2);
                            pill.title = resultSet.getString(3);
                            pill.body = resultSet.getString(4);
                            pill.questiontype = resultSet.getString(5);
                            if (!resultSet.getString(5).equals("NONE"))
                                pill.isQuestion = true;
                            else
                                pill.isQuestion = false;

                            String options = resultSet.getString(7);
                            pill.question = new HashMap();
                            pill.question.put("questiontext", resultSet.getString(6));
                            String[] arr = new String[0];
                            if (options != null)
                                pill.question.put("answeroptions", options.split(","));
                            else
                                pill.question.put("answeroptions", arr);

                            pill.question.put("answertext", resultSet.getString(8));
                            pill.question.put("scorecorrect", resultSet.getString(9));
                            pill.question.put("scoreincorrect", resultSet.getDouble(10));
                            pill.products = (Integer[]) resultSet.getArray(11).getArray();
                            pill.keywords = (String[]) resultSet.getArray(12).getArray();
                            pill.createdate = resultSet.getTimestamp(13);
                            pill.createby = resultSet.getInt(14);
                            pill.pillImages = new ArrayList<>();
                            pill.pillResizeImages = new ArrayList<>();

                            stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, mediatype " +
                                    " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                            stmt.setInt(1, resultSet.getInt(1));
                            contentSet = stmt.executeQuery();
                            while (contentSet.next()) {

                                if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                    pill.isImage = true;
                                    pill.pillImages.add(contentSet.getString(2));
                                    pill.pillResizeImages.add(contentSet.getString(3));
                                }

                                if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                    pill.isVideo = true;
                                    pill.pillVideo = contentSet.getString(2);
                                }
                            }

                            pillList.add(pill);
                        }
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
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param feedDeliveryId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> getAllDeliveredPills(int feedDeliveryId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Pill> pillList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet result = null;
            ResultSet resultSet = null;
            ResultSet contentSet = null;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT id,pillid,deliverytime,readtime,answerjson FROM " + schemaName + ".feeddelivery WHERE userid = ? AND id > ?");
                    stmt.setInt(1, loggedInUser.id);
                    stmt.setInt(2, feedDeliveryId);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Pill pill = new Pill();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                        String date = sdf.format(result.getTimestamp(3));
                        pill.feedDeliveryId = result.getInt(1);
                        pill.deliveryTime = date;

                        pill.readTime = result.getTimestamp(4);
                        pill.userAnswerJson = result.getString(5);

                        stmt = con.prepareStatement(" SELECT id, divid, title, body, questiontype, questiontext, answeroptions, " +
                                " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby " +
                                " FROM " + schemaName + ".pills p " +
                                " WHERE id = ? ");
                        stmt.setInt(1, result.getInt(2));
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {

                            pill.id = resultSet.getInt(1);
                            pill.divid = resultSet.getInt(2);
                            pill.title = resultSet.getString(3);
                            pill.body = resultSet.getString(4);
                            pill.questiontype = resultSet.getString(5);
                            if (!resultSet.getString(5).equals("NONE"))
                                pill.isQuestion = true;
                            else
                                pill.isQuestion = false;

                            String options = resultSet.getString(7);
                            pill.question = new HashMap();
                            pill.question.put("questiontext", resultSet.getString(6));
                            String[] arr = new String[0];
                            if (options != null)
                                pill.question.put("answeroptions", options.split(","));
                            else
                                pill.question.put("answeroptions", arr);

                            pill.question.put("answertext", resultSet.getString(8));
                            pill.question.put("scorecorrect", resultSet.getString(9));
                            pill.question.put("scoreincorrect", resultSet.getDouble(10));
                            pill.products = (Integer[]) resultSet.getArray(11).getArray();
                            pill.keywords = (String[]) resultSet.getArray(12).getArray();
                            pill.createdate = resultSet.getTimestamp(13);
                            pill.createby = resultSet.getInt(14);
                            pill.pillImages = new ArrayList<>();
                            pill.pillResizeImages = new ArrayList<>();

                            stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, mediatype " +
                                    " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                            stmt.setInt(1, resultSet.getInt(1));
                            contentSet = stmt.executeQuery();
                            while (contentSet.next()) {

                                if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                    pill.isImage = true;
                                    pill.pillImages.add(contentSet.getString(2));
                                    pill.pillResizeImages.add(contentSet.getString(3));
                                }

                                if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                    pill.isVideo = true;
                                    pill.pillVideo = contentSet.getString(2);
                                }
                            }

                            pillList.add(pill);
                        }
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
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * @param pillId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Pill getPillById(int pillId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            Pill pill = null;
            String schemaName = loggedInUser.schemaName;
            ResultSet result = null;
            ResultSet resultSet = null;
            ResultSet contentSet = null;

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT id, divid, title, body, questiontype, questiontext, answeroptions, " +
                            " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby " +
                            " FROM " + schemaName + ".pills p " +
                            " WHERE id = ? ");
                    stmt.setInt(1, pillId);
                    resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        pill = new Pill();
                        pill.id = resultSet.getInt(1);
                        pill.divid = resultSet.getInt(2);
                        pill.title = resultSet.getString(3);
                        pill.body = resultSet.getString(4);
                        pill.questiontype = resultSet.getString(5);
                        if (!resultSet.getString(5).equals("NONE"))
                            pill.isQuestion = true;
                        else
                            pill.isQuestion = false;

                        String options = resultSet.getString(7);
                        pill.question = new HashMap();
                        pill.question.put("questiontext", resultSet.getString(6));

                        String[] arr = new String[0];
                        if (options != null)
                            pill.question.put("answeroptions", options.split(","));
                        else
                            pill.question.put("answeroptions", arr);

                        pill.question.put("answertext", resultSet.getString(8));
                        pill.question.put("scorecorrect", resultSet.getString(9));
                        pill.question.put("scoreincorrect", resultSet.getDouble(10));
                        pill.products = (Integer[]) resultSet.getArray(11).getArray();
                        pill.keywords = (String[]) resultSet.getArray(12).getArray();
                        pill.createdate = resultSet.getTimestamp(13);
                        pill.createby = resultSet.getInt(14);
                        pill.pillImages = new ArrayList<>();
                        pill.pillResizeImages = new ArrayList<>();

                        stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                " videothumbnailurl, mediatype " +
                                " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                        stmt.setInt(1, pillId);
                        contentSet = stmt.executeQuery();
                        while (contentSet.next()) {

                            if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                pill.isImage = true;
                                pill.pillImages.add(contentSet.getString(2));
                                pill.pillResizeImages.add(contentSet.getString(3));
                            }

                            if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                pill.isVideo = true;
                                pill.pillVideo = contentSet.getString(2);
                            }
                        }

                        stmt = con.prepareStatement(" SELECT deliverytime,readtime,answerjson,id FROM " + schemaName + ".feeddelivery " +
                                " WHERE pillid = ? AND userid = ? ");
                        stmt.setInt(1, pillId);
                        stmt.setInt(2, loggedInUser.id);
                        contentSet = stmt.executeQuery();
                        while (contentSet.next()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
                            String date = sdf.format(contentSet.getTimestamp(1));

                            pill.deliveryTime = date;
                            pill.readTime = contentSet.getTimestamp(2);
                            pill.userAnswerJson = contentSet.getString(3);
                        }

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
            return pill;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> getAllPillsByDivision(int divId, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Pill> pillList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet = null;
            ResultSet contentSet = null;

            try {
                if (con != null) {
                    if (divId != -1) {
                        stmt = con.prepareStatement(" SELECT p.id, p.divid, title, body, questiontype, questiontext, answeroptions, " +
                                " answertext, scorecorrect, scoreincorrect, products, keywords, p.createdate, p.createby," +
                                " u.username,u.firstname,u.lastname,(uf.address).city,(uf.address).state,(uf.address).phone,d.name " +
                                " FROM (select * from " + schemaName + ".pills p WHERE divid = ?)p " +
                                " left join master.users u on u.id = p.createby " +
                                " left join " + schemaName + ".userprofile uf on uf.userid = p.createby " +
                                " left join " + schemaName + ".divisions d on d.id = p.divid " +
                                " ORDER BY p.createdate DESC");
                        stmt.setInt(1, divId);
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            Pill pill = new Pill();
                            pill.id = resultSet.getInt(1);
                            pill.divid = resultSet.getInt(2);
                            pill.title = resultSet.getString(3);
                            pill.body = resultSet.getString(4);
                            pill.questiontype = resultSet.getString(5);
                            if (!resultSet.getString(5).equals("NONE"))
                                pill.isQuestion = true;
                            else
                                pill.isQuestion = false;

                            String options = resultSet.getString(7);

                            pill.question = new HashMap();
                            pill.question.put("questiontext", resultSet.getString(6));

                            String[] arr = new String[0];
                            if (options != null)
                                pill.question.put("answeroptions", options.split(","));
                            else
                                pill.question.put("answeroptions", arr);

                            pill.question.put("answertext", resultSet.getString(8));
                            pill.question.put("scorecorrect", resultSet.getString(9));
                            pill.question.put("scoreincorrect", resultSet.getDouble(10));
                            pill.products = (Integer[]) resultSet.getArray(11).getArray();
                            pill.keywords = (String[]) resultSet.getArray(12).getArray();
                            pill.createdate = resultSet.getTimestamp(13);
                            pill.createby = resultSet.getInt(14);
                            pill.userDetails = new ArrayList<>();
                            pill.userDetails.add(new UserDetail(resultSet.getInt(14), resultSet.getString(15), resultSet.getString(16), resultSet.getString(17), resultSet.getString(18), resultSet.getString(19), (String[]) resultSet.getArray(20).getArray()));
                            pill.divName = resultSet.getString(21);

                            pill.pillImages = new ArrayList<>();

                            stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, mediatype " +
                                    " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                            stmt.setInt(1, resultSet.getInt(1));
                            contentSet = stmt.executeQuery();
                            while (contentSet.next()) {

                                if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                    pill.isImage = true;
                                    pill.pillImages.add(contentSet.getString(2));
                                }

                                if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                    pill.isVideo = true;
                                    pill.pillVideo = contentSet.getString(2);
                                }
                            }
                            pillList.add(pill);
                        }
                    } else {
                        stmt = con.prepareStatement(" SELECT p.id, p.divid, title, body, questiontype, questiontext, answeroptions, " +
                                " answertext, scorecorrect, scoreincorrect, products, keywords, p.createdate, p.createby," +
                                " u.username,u.firstname,u.lastname,(uf.address).city,(uf.address).state,(uf.address).phone,d.name " +
                                " FROM " + schemaName + ".pills p " +
                                " left join master.users u on u.id = p.createby " +
                                " left join " + schemaName + ".userprofile uf on uf.userid = p.createby " +
                                " left join " + schemaName + ".divisions d on d.id = p.divid " +
                                " ORDER BY p.createdate DESC");
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            Pill pill = new Pill();
                            pill.id = resultSet.getInt(1);
                            pill.divid = resultSet.getInt(2);
                            pill.title = resultSet.getString(3);
                            pill.body = resultSet.getString(4);
                            pill.questiontype = resultSet.getString(5);
                            if (!resultSet.getString(5).equals("NONE"))
                                pill.isQuestion = true;
                            else
                                pill.isQuestion = false;

                            String options = resultSet.getString(7);
                            pill.question = new HashMap();
                            pill.question.put("questiontext", resultSet.getString(6));

                            String[] arr = new String[0];
                            if (options != null)
                                pill.question.put("answeroptions", options.split(","));
                            else
                                pill.question.put("answeroptions", arr);

                            pill.question.put("answertext", resultSet.getString(8));
                            pill.question.put("scorecorrect", resultSet.getString(9));
                            pill.question.put("scoreincorrect", resultSet.getDouble(10));
                            pill.products = (Integer[]) resultSet.getArray(11).getArray();
                            pill.keywords = (String[]) resultSet.getArray(12).getArray();
                            pill.createdate = resultSet.getTimestamp(13);
                            pill.createby = resultSet.getInt(14);
                            pill.userDetails = new ArrayList<>();
                            pill.userDetails.add(new UserDetail(resultSet.getInt(14), resultSet.getString(15), resultSet.getString(16), resultSet.getString(17), resultSet.getString(18), resultSet.getString(19), (String[]) resultSet.getArray(20).getArray()));
                            pill.divName = resultSet.getString(21);

                            pill.pillImages = new ArrayList<>();

                            stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, mediatype " +
                                    " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                            stmt.setInt(1, resultSet.getInt(1));
                            contentSet = stmt.executeQuery();
                            while (contentSet.next()) {

                                if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                    pill.isImage = true;
                                    pill.pillImages.add(contentSet.getString(2));
                                }

                                if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                    pill.isVideo = true;
                                    pill.pillVideo = contentSet.getString(2);
                                }
                            }
                            pillList.add(pill);
                        }
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
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> getAllPillsOfUser(LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Pill> pillList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet = null;
            ResultSet contentSet = null;
            ResultSet pillResultSet = null;
            Integer[] pillsId = new Integer[0];

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT id, divid, title, body, questiontype, questiontext, answeroptions, " +
                            " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby " +
                            " FROM " + schemaName + ".pills p WHERE id = ANY((select pills from " + schemaName + ".feeds f " +
                            " left join " + schemaName + ".feedschedule fs on fs.feedid = f.id " +
                            " where ? = ANY(userid :: int[])) :: int[]) ORDER BY createdate DESC");
                    stmt.setInt(1, loggedInUser.id);
                    pillResultSet = stmt.executeQuery();
                    while (pillResultSet.next()) {
                        Pill pill = new Pill();
                        pill.id = pillResultSet.getInt(1);
                        pill.divid = pillResultSet.getInt(2);
                        pill.title = pillResultSet.getString(3);
                        pill.body = pillResultSet.getString(4);
                        pill.questiontype = pillResultSet.getString(5);
                        if (!pillResultSet.getString(5).equals("NONE"))
                            pill.isQuestion = true;
                        else
                            pill.isQuestion = false;

                        String options = pillResultSet.getString(7);
                        pill.question = new HashMap();
                        pill.question.put("questiontext", pillResultSet.getString(6));

                        String[] arr = new String[0];
                        if (options != null)
                            pill.question.put("answeroptions", options.split(","));
                        else
                            pill.question.put("answeroptions", arr);

                        pill.question.put("answertext", pillResultSet.getString(8));
                        pill.question.put("scorecorrect", pillResultSet.getString(9));
                        pill.question.put("scoreincorrect", pillResultSet.getDouble(10));
                        pill.products = (Integer[]) pillResultSet.getArray(11).getArray();
                        pill.keywords = (String[]) pillResultSet.getArray(12).getArray();
                        pill.createdate = pillResultSet.getTimestamp(13);
                        pill.createby = pillResultSet.getInt(14);
                        pill.pillImages = new ArrayList<>();

                        stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                " videothumbnailurl, mediatype " +
                                " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                        stmt.setInt(1, pillResultSet.getInt(1));
                        contentSet = stmt.executeQuery();
                        while (contentSet.next()) {

                            if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                pill.isImage = true;
                                pill.pillImages.add(contentSet.getString(2));
                            }

                            if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                pill.isVideo = true;
                                pill.pillVideo = contentSet.getString(2);
                            }
                        }
                        pillList.add(pill);
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
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param feedId
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> getAllPillsOfFeed(int feedId, LoggedInUser loggedInUser) throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            List<Pill> pillList = new ArrayList<>();
            String schemaName = loggedInUser.schemaName;
            ResultSet contentSet = null;
            ResultSet pillResultSet = null;
            Integer[] pillsId = new Integer[0];

            try {
                if (con != null) {

                    stmt = con.prepareStatement(" SELECT p.id, p.divid, title, body, questiontype, questiontext, answeroptions, " +
                            " answertext, scorecorrect, scoreincorrect, products, keywords, p.createdate, p.createby,u.username,u.firstname," +
                            " u.lastname,(uf.address).city, (uf.address).state, (uf.address).phone " +
                            " FROM (select * from " + schemaName + ".feeds f WHERE f.id = ?)f " +
                            " left join " + schemaName + ".pills p on p.id = ANY(pills :: int[]) " +
                            " left join master.users u on u.id = p.createby" +
                            " left join " + schemaName + ".userprofile uf on uf.userid = p.createby  " +
                            " ORDER BY p.createdate DESC ");
                    stmt.setInt(1, feedId);
                    pillResultSet = stmt.executeQuery();
                    while (pillResultSet.next()) {
                        if (pillResultSet.getInt(1) > 0) {
                            Pill pill = new Pill();
                            pill.id = pillResultSet.getInt(1);
                            pill.divid = pillResultSet.getInt(2);
                            pill.title = pillResultSet.getString(3);
                            pill.body = pillResultSet.getString(4);
                            pill.questiontype = pillResultSet.getString(5);

                            if (pillResultSet.getString(5) != null) {
                                if (!pillResultSet.getString(5).equals("NONE"))
                                    pill.isQuestion = true;
                                else
                                    pill.isQuestion = false;
                            }

                            String options = pillResultSet.getString(7);
                            pill.question = new HashMap();
                            pill.question.put("questiontext", pillResultSet.getString(6));

                            String[] arr = new String[0];
                            if (options != null)
                                pill.question.put("answeroptions", options.split(","));
                            else
                                pill.question.put("answeroptions", arr);

                            pill.question.put("answertext", pillResultSet.getString(8));
                            pill.question.put("scorecorrect", pillResultSet.getString(9));
                            pill.question.put("scoreincorrect", pillResultSet.getDouble(10));

                            if (pillResultSet.getArray(11) != null) {
                                pill.products = (Integer[]) pillResultSet.getArray(11).getArray();
                            }

                            if (pillResultSet.getArray(12) != null) {
                                pill.keywords = (String[]) pillResultSet.getArray(12).getArray();
                            }
                            pill.createdate = pillResultSet.getTimestamp(13);
                            pill.createby = pillResultSet.getInt(14);
                            pill.userDetails = new ArrayList<>();
                            pill.userDetails.add(new UserDetail(pillResultSet.getInt(14), pillResultSet.getString(15), pillResultSet.getString(16),
                                    pillResultSet.getString(17), pillResultSet.getString(18),
                                    pillResultSet.getString(19), (String[]) pillResultSet.getArray(20).getArray()));

                            pill.pillImages = new ArrayList<>();

                            stmt = con.prepareStatement(" SELECT pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, mediatype " +
                                    " FROM " + schemaName + ".pillsmedia WHERE pillid = ?  ");
                            stmt.setInt(1, pillResultSet.getInt(1));
                            contentSet = stmt.executeQuery();
                            while (contentSet.next()) {

                                if (contentSet.getString(6).equalsIgnoreCase("image")) {
                                    pill.isImage = true;
                                    pill.pillImages.add(contentSet.getString(2));
                                }

                                if (contentSet.getString(6).equalsIgnoreCase("video")) {
                                    pill.isVideo = true;
                                    pill.pillVideo = contentSet.getString(2);
                                }
                            }
                            pillList.add(pill);

                        }
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
                if (pillResultSet != null)
                    if (!pillResultSet.isClosed())
                        pillResultSet.close();
                if (contentSet != null)
                    if (!contentSet.isClosed())
                        contentSet.close();
            }
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param divid
     * @param title
     * @param body
     * @param questiontype
     * @param questiontext
     * @param answeroptions
     * @param answertext
     * @param scorecorrect
     * @param scoreincorrect
     * @param products
     * @param keywords
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addPills(int divid, String title, String body, String questiontype, String questiontext,
                               String answeroptions, String answertext, int scorecorrect, String scoreincorrect,
                               String products, String keywords, List<String> filePath, List<String> fileTypes,
                               List<String> resizeList, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            String schemaname = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            ResultSet resultSet;
            int pillId = 0;

            try {
                con.setAutoCommit(false);
                QuestionType quesType;
                if (!questiontype.isEmpty() && questiontype != null) {
                    quesType = QuestionType.valueOf(questiontype.toUpperCase());
                } else {
                    quesType = QuestionType.NONE;
                }

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

                stmt = con.prepareStatement(" SELECT Count(*) FROM " + schemaname + ".pills WHERE title = ? ");
                stmt.setString(1, title);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getInt(1) == 0) {
                        stmt = con.prepareStatement(" INSERT INTO " + schemaname
                                + ".pills(divid, title, body, questiontype, questiontext, answeroptions," +
                                " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby) " +
                                " VALUES (?,?,?,CAST(? AS master.questiontype),?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

                        stmt.setInt(1, divid);
                        stmt.setString(2, title);

                        if (!body.isEmpty() && body != null)
                            stmt.setString(3, body);
                        else
                            stmt.setString(3, null);

                        stmt.setString(4, quesType.name());

                        if (!questiontext.isEmpty() && questiontext != null)
                            stmt.setString(5, questiontext);
                        else
                            stmt.setString(5, null);

                        if (!answeroptions.isEmpty() && answeroptions != null)
                            stmt.setString(6, answeroptions);
                        else
                            stmt.setString(6, null);

                        if (!answertext.isEmpty() && answertext != null)
                            stmt.setString(7, answertext);
                        else
                            stmt.setString(7, null);

                        stmt.setInt(8, scorecorrect);

                        stmt.setDouble(9, Double.parseDouble(scoreincorrect));

                        stmt.setArray(10, prdarr);

                        stmt.setArray(11, keyArr);

                        stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));

                        stmt.setInt(13, loggedInUser.id);

                        result = stmt.executeUpdate();

                        if (result == 0)
                            throw new SQLException("Add Pills Failed.");

                        ResultSet generatedKeys = stmt.getGeneratedKeys();
                        if (generatedKeys.next())
                            // It gives last inserted Id in questionId
                            pillId = generatedKeys.getInt(1);
                        else
                            throw new SQLException("No ID obtained");

                        for (int i = 0; i < filePath.size(); i++) {
                            stmt = con.prepareStatement("INSERT INTO " + schemaname
                                    + ".pillsmedia(pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                    " videothumbnailurl, createdate, createby,mediatype) VALUES (?,?,?,?,?,?,?,?)");

                            stmt.setInt(1, pillId);
                            stmt.setString(2, filePath.get(i));
                            stmt.setString(3, resizeList.get(i));
                            stmt.setString(4, "");
                            stmt.setString(5, "");
                            stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                            stmt.setInt(7, loggedInUser.id);

                            if (!fileTypes.get(i).isEmpty() || !fileTypes.get(i).equals("") || !fileTypes.get(i).equals(null))
                                stmt.setString(8, fileTypes.get(i));
                            else
                                stmt.setString(8, "none");

                            stmt.executeUpdate();
                        }
                    } else
                        throw new BadRequestException("Pill is alredy Exist with same name");
                }

                con.commit();
                return pillId;
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * @param divid
     * @param title
     * @param body
     * @param questiontype
     * @param questiontext
     * @param answeroptions
     * @param answertext
     * @param scorecorrect
     * @param scoreincorrect
     * @param products
     * @param keywords
     * @param filePath
     * @param fileTypes
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updatePills(int divid, String title, String body, String questiontype, String questiontext,
                                  String answeroptions, String answertext, int scorecorrect, String scoreincorrect,
                                  String products, String keywords, List<String> filePath, List<String> fileTypes,
                                  List<String> resizeList, boolean isUpdate, int id,
                                  LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            String schemaname = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                QuestionType quesType;
                if (!questiontype.isEmpty() && questiontype != null) {
                    quesType = QuestionType.valueOf(questiontype.toUpperCase());
                } else {
                    quesType = QuestionType.NONE;
                }

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

                stmt = con.prepareStatement(" UPDATE " + schemaname + ".pills " +
                        " SET divid=?, title=?, body=?, questiontype= CAST(? AS master.questiontype), questiontext=?, answeroptions=?," +
                        " answertext=?, scorecorrect=?, scoreincorrect=?, products=?, keywords=? " +
                        " WHERE id = ? ");

                stmt.setInt(1, divid);
                stmt.setString(2, title);

                if (!body.isEmpty() && body != null)
                    stmt.setString(3, body);
                else
                    stmt.setString(3, null);

                stmt.setString(4, quesType.name());

                if (!questiontext.isEmpty() && questiontext != null)
                    stmt.setString(5, questiontext);
                else
                    stmt.setString(5, null);

                if (!answeroptions.isEmpty() && answeroptions != null)
                    stmt.setString(6, answeroptions);
                else
                    stmt.setString(6, null);

                if (!answertext.isEmpty() && answertext != null)
                    stmt.setString(7, answertext);
                else
                    stmt.setString(7, null);

                stmt.setInt(8, scorecorrect);

                stmt.setDouble(9, Double.parseDouble(scoreincorrect));

                stmt.setArray(10, prdarr);

                stmt.setArray(11, keyArr);

                stmt.setInt(12, id);

                result = stmt.executeUpdate();

                if (isUpdate) {
                    stmt = con.prepareStatement(" DELETE FROM " + schemaname + ".pillsmedia WHERE pillid = ? ");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();

                    for (int i = 0; i < filePath.size(); i++) {
                        stmt = con.prepareStatement("INSERT INTO " + schemaname
                                + ".pillsmedia(pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                                " videothumbnailurl, createdate, createby,mediatype) VALUES (?,?,?,?,?,?,?,?)");

                        stmt.setInt(1, id);
                        stmt.setString(2, filePath.get(i));
                        stmt.setString(3, resizeList.get(i));
                        stmt.setString(4, "");
                        stmt.setString(5, "");
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);

                        if (!fileTypes.get(i).isEmpty() || !fileTypes.get(i).equals("") || !fileTypes.get(i).equals(null))
                            stmt.setString(8, fileTypes.get(i));
                        else
                            stmt.setString(8, "none");

                        stmt.executeUpdate();
                    }
                }
                return result;
            } finally {
                if (con != null)
                    if (!con.isClosed())
                        con.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
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
    public static int deletePills(int id, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            String schemaName = loggedInUser.schemaName;
            ResultSet resultSet;

            try {

                stmt = con.prepareStatement("Select SUM(cnt) FROM (" +
                        " Select count(*) cnt from " + schemaName + ".feeds where ? = ANY(pills :: int[]) " +
                        " union all " +
                        " Select count(*) cnt from " + schemaName + ".feeddelivery where pillid = ? " +
                        " union all " +
                        " Select count(*) cnt from " + schemaName + ".feeddeliveryfail where pillid = ?) " +
                        " AS TotalCount");

                stmt.setInt(1, id);
                stmt.setInt(2, id);
                stmt.setInt(3, id);
                resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getInt(1) == 0) {
                        stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".pillsmedia WHERE pillid = ? ");
                        stmt.setInt(1, id);
                        affectedRow = stmt.executeUpdate();
                    }
                    stmt = con.prepareStatement(" DELETE FROM " + schemaName + ".pills WHERE id = ? ");
                    stmt.setInt(1, id);
                    affectedRow = stmt.executeUpdate();
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
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Pill> filterPills(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Pill).equals("Read") ||
                Permissions.isAuthorised(userRole, Pill).equals("Write")) {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            String schemaName = loggedInUser.schemaName;
            List<Pill> pillList = new ArrayList<>();
            boolean isKeyword = false, isProduct = false, isAuthor = false, isPillType = false;
            int index = 1;
            ResultSet result = null;

            try {
                if (con != null) {
                    String query = "SELECT p.id, divid, title, body, questiontype, questiontext, answeroptions, answertext," +
                            "  scorecorrect, scoreincorrect, products, keywords, createdate, createby " +
                            " FROM " + schemaName + ".pills p WHERE divid = ? ";

                    if (node.has("keywords")) {
                        if (node.withArray("keywords").size() > 0) {
                            query = query.concat(" AND keywords = ? ");
                            isKeyword = true;
                        }
                    }

                    if (node.has("product")) {
                        if (node.withArray("product").size() > 0) {
                            query = query.concat(" AND products = ? ");
                            isProduct = true;
                        }
                    }

                    if (node.has("author")) {
                        if (node.get("author").asInt() > 0) {
                            query = query.concat(" AND createby = ? ");
                            isAuthor = true;
                        }
                    }

                    if (node.has("pillType")) {
                        if (node.get("pillType").asText().equals("Question")) {
                            isPillType = true;
                            query = query.concat(" AND questiontext IS NOT NULL ");
                        }

                        if (node.get("pillType").asText().equals("Info")) {
                            isPillType = true;
                            query = query.concat(" AND NOT(questiontext IS NOT NULL)");
                        }
                    }

                    query = query.concat(" AND NOT EXISTS (SELECT pills FROM " + schemaName + ".feeds f WHERE f.id = ? AND p.id = ANY(pills :: int[]))");

                    stmt = con.prepareStatement(query);
                    System.out.println(" Query : " + query);

                    stmt.setInt(index++, node.get("divId").asInt());

                    if (isKeyword) {
                        String arr[] = new String[node.withArray("keywords").size()];
                        for (int i = 0; i < node.withArray("keywords").size(); i++) {
                            arr[i] = node.withArray("keywords").get(i).asText();
                        }
                        Array array = con.createArrayOf("text", arr);
                        stmt.setArray(index++, array);
                    }

                    if (isProduct) {
                        Integer arr[] = new Integer[node.withArray("product").size()];
                        for (int i = 0; i < node.withArray("product").size(); i++) {
                            arr[i] = node.withArray("product").get(i).asInt();
                        }
                        Array array = con.createArrayOf("int", arr);
                        stmt.setArray(index++, array);
                    }
                    if (isAuthor) {
                        stmt.setInt(index++, node.get("author").asInt());
                    }

                    stmt.setInt(index++, node.get("feedId").asInt());

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Pill pill = new Pill();
                        pill.id = result.getInt(1);
                        pill.divid = result.getInt(2);
                        pill.title = result.getString(3);
                        pill.body = result.getString(4);
                        pill.questiontype = result.getString(5);
                        if (!result.getString(5).equals("NONE"))
                            pill.isQuestion = true;
                        else
                            pill.isQuestion = false;

                        String options = result.getString(7);
                        pill.question = new HashMap();
                        pill.question.put("questiontext", result.getString(6));
                        if (options != null)
                            pill.question.put("answeroptions", options.split(","));

                        pill.question.put("answertext", result.getString(8));
                        pill.question.put("scorecorrect", result.getString(9));
                        pill.question.put("scoreincorrect", result.getDouble(10));
                        pill.products = (Integer[]) result.getArray(11).getArray();
                        pill.keywords = (String[]) result.getArray(12).getArray();
                        pill.createdate = result.getTimestamp(13);
                        pill.createby = result.getInt(14);
                        pillList.add(pill);
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
            }
            return pillList;
        } else {
            throw new NotAuthorizedException("");
        }
    }

}
