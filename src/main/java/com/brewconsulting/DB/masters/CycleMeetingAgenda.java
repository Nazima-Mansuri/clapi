package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.Interval;

import javax.naming.NamingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class CycleMeetingAgenda {

    @JsonProperty("id")
    public int id;

    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonProperty("meetingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date meetingDate;

    @JsonProperty("sessionName")
    public String sessionName;

    @JsonProperty("sessionDesc")
    public String sessionDesc;

    @JsonProperty("sessionStartTime")
    public String sessionStartTime;

    @JsonProperty("sessionEndTime")
    public String sessionEndTime;

    @JsonProperty("sessionConductor")
    public String sessionConductor;

    @JsonProperty("contentType")
    public String contentType;

    @JsonProperty("createOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createOn;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updatedOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updatedOn;

    @JsonProperty("updateBy")
    public int updatedBy;

    @JsonProperty("contentList")
    public  List<Content> contentList;

    @JsonProperty("questionCollectionList")
    public List<QuestionCollection> questionCollectionList;

    @JsonProperty("testSettingList")
    public List<QuestionCollection> testSettingList;

    public static final int CycleMeetingAgenda = 10;

    // make the default constructor visible to package only.
    CycleMeetingAgenda() {

    }

    public enum ContentType {
        ACTIVITY, INFO, TEST, MIXED;
    }

    /***
     * Method is used to clone child agenda from parent agenda.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws SQLException
     * @throws NamingException
     * @throws ClassNotFoundException
     */
    public static List<Integer> cloneCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas  = new ArrayList<>();;
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet resultSet = null;
            ResultSet testResultSet = null;
            int affectedRows;
            int meetingid = 0;
            List<Integer> idList = new ArrayList<>();
            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT g.id,sessionname, sessiondesc, sessionstarttime," +
                        " sessionendtime,g.contenttype,sessionconductor " +
                        " FROM " + schemaName + ".groupagenda g " +
                        " where groupid = ? and dayNo = ?");
                stmt.setInt(1, node.get("groupid").asInt());
                stmt.setInt(2, node.get("dayNo").asInt());
                result = stmt.executeQuery();
                while (result.next()) {
                    CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.sessionName = result.getString(2);
                        cycleMeetingAgenda.sessionDesc = result.getString(3);
                        cycleMeetingAgenda.sessionStartTime = String.valueOf(result.getTime(4));
                        cycleMeetingAgenda.sessionEndTime = String.valueOf(result.getTime(5));
                        cycleMeetingAgenda.contentType = result.getString(6);
                        cycleMeetingAgenda.sessionConductor = result.getString(7);
                        cycleMeetingAgenda.contentList = new ArrayList<>();
                        cycleMeetingAgenda.questionCollectionList = new ArrayList<>();
                        cycleMeetingAgenda.testSettingList = new ArrayList<>();

                    if(result.getString(6).equals("INFO") || result.getString(6).equals("ACTIVITY")) {
                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, contentid,title,description " +
                                " FROM " + schemaName + ".groupsessioncontentinfo WHERE agendaid = ?");
                        stmt.setInt(1, result.getInt(1));
                        resultSet = stmt.executeQuery();
                        while (resultSet.next()) {
                            Content content = new Content();
                            content.id = resultSet.getInt(1);
                            content.agendaId = resultSet.getInt(2);
                            content.contentType = resultSet.getString(3);
                            content.contentSeq = resultSet.getInt(4);
                            content.contentId = (Integer[]) resultSet.getArray(5).getArray();
                            content.title = resultSet.getString(6);
                            content.description = resultSet.getString(7);
                            cycleMeetingAgenda.contentList.add(content);
                        }
                    }

                    if(result.getString(6).equals("TEST"))
                    {
                        stmt = con.prepareStatement(" SELECT id, agendaid, contenttype, contentseq, " +
                                " questions, randomdelivery, collectionseq, " +
                                " disregardcomplexitylevel, deliverallquestions, questionbreakup, " +
                                " collectionname, title, description " +
                                " FROM "+schemaName+".groupsessioncontenttestquestioncollection " +
                                " WHERE agendaid = ? ");
                        stmt.setInt(1,result.getInt(1));
                        resultSet = stmt.executeQuery();
                        while (resultSet.next())
                        {
                            QuestionCollection collection = new QuestionCollection();
                            collection.id = resultSet.getInt(1);
                            collection.agendaId = resultSet.getInt(2);
                            collection.contentType = resultSet.getString(3);
                            collection.contentSeq = resultSet.getInt(4);
                            collection.questionsId = (Integer[]) resultSet.getArray(5).getArray();
                            collection.randomdelivery = resultSet.getBoolean(6);
                            collection.collectionseq = resultSet.getInt(7);
                            collection.disregardcomplexitylevel = resultSet.getBoolean(8);
                            collection.deliverallquestions = resultSet.getBoolean(9);
                            collection.questionbreakup = (Integer[]) resultSet.getArray(10).getArray();
                            collection.collection = resultSet.getString(11);
                            collection.title = resultSet.getString(12);
                            collection.description = resultSet.getString(13);
                            cycleMeetingAgenda.questionCollectionList.add(collection);
                        }

                        stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq," +
                                " testinstruction, testendnote, applyscoring," +
                                " scorecorrect, showfeedback, duration, applyinterval, timeperquestion, " +
                                " applytimeperquestion, allowreview, title, description, testdescription,scoreincorrect " +
                                " FROM "+schemaName+".groupsessioncontenttest WHERE agendaid = ?  ");
                        stmt.setInt(1,result.getInt(1));
                        resultSet = stmt.executeQuery();
                        while (resultSet.next())
                        {
                            QuestionCollection testCollection = new QuestionCollection();
                            testCollection.id = resultSet.getInt(1);
                            testCollection.agendaId = resultSet.getInt(2);
                            testCollection.contentType = resultSet.getString(3);
                            testCollection.contentSeq = resultSet.getInt(4);
                            testCollection.Instrction = resultSet.getString(5);
                            testCollection.EndNote = resultSet.getString(6);
                            testCollection.IsApplyScoring = resultSet.getBoolean(7);
                            testCollection.scorecorrect = (Integer[]) resultSet.getArray(8).getArray();
                            testCollection.showFeedBack = resultSet.getBoolean(9);
                            testCollection.duration = resultSet.getString(10);
                            testCollection.applyInterval = resultSet.getBoolean(11);
                            testCollection.differentTime = (Integer[]) resultSet.getArray(12).getArray();
                            testCollection.applytimeperquestion = resultSet.getBoolean(13);
                            testCollection.AllowReview = resultSet.getBoolean(14);
                            testCollection.title = resultSet.getString(15);
                            testCollection.description = resultSet.getString(16);
                            System.out.println("TEST ttile : " + resultSet.getString(15));
                            System.out.println("TEST desc : " +  resultSet.getString(16));
                            testCollection.Description = resultSet.getString(17);
                            testCollection.scoreIncorrect = (Double[]) resultSet.getArray(18).getArray();
                            cycleMeetingAgenda.testSettingList.add(testCollection);
                        }
                    }

                    // If Session Type is MIXED then it get all Items of INFO,ACTIVITY and TEST.
                    if(result.getString(6).equals("MIXED"))
                    {
                            stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq, contentid,title,description " +
                                    " FROM " + schemaName + ".groupsessioncontentinfo WHERE agendaid = ?");
                            stmt.setInt(1, result.getInt(1));
                            resultSet = stmt.executeQuery();
                            while (resultSet.next()) {
                                Content content = new Content();
                                content.id = resultSet.getInt(1);
                                content.agendaId = resultSet.getInt(2);
                                content.contentType = resultSet.getString(3);
                                content.contentSeq = resultSet.getInt(4);
                                content.contentId = (Integer[]) resultSet.getArray(5).getArray();
                                content.title = resultSet.getString(6);
                                content.description = resultSet.getString(7);
                                cycleMeetingAgenda.contentList.add(content);
                            }

                            stmt = con.prepareStatement(" SELECT id, agendaid, contenttype, contentseq, " +
                                    " questions, randomdelivery, collectionseq, " +
                                    " disregardcomplexitylevel, deliverallquestions, questionbreakup, " +
                                    " collectionname, title, description " +
                                    " FROM "+schemaName+".groupsessioncontenttestquestioncollection " +
                                    " WHERE agendaid = ? ");
                            stmt.setInt(1,result.getInt(1));
                            resultSet = stmt.executeQuery();
                            while (resultSet.next())
                            {
                                QuestionCollection collection = new QuestionCollection();
                                collection.id = resultSet.getInt(1);
                                collection.agendaId = resultSet.getInt(2);
                                collection.contentType = resultSet.getString(3);
                                collection.contentSeq = resultSet.getInt(4);
                                collection.questionsId = (Integer[]) resultSet.getArray(5).getArray();
                                collection.randomdelivery = resultSet.getBoolean(6);
                                collection.collectionseq = resultSet.getInt(7);
                                collection.disregardcomplexitylevel = resultSet.getBoolean(8);
                                collection.deliverallquestions = resultSet.getBoolean(9);
                                collection.questionbreakup = (Integer[]) resultSet.getArray(10).getArray();
                                collection.collection = resultSet.getString(11);
                                collection.title = resultSet.getString(12);
                                collection.description = resultSet.getString(13);
                                cycleMeetingAgenda.questionCollectionList.add(collection);
                            }

                            stmt = con.prepareStatement("SELECT id, agendaid, contenttype, contentseq," +
                                    " testinstruction, testendnote, applyscoring," +
                                    " scorecorrect, showfeedback, duration, applyinterval, timeperquestion, " +
                                    " applytimeperquestion, allowreview, title, description, testdescription,scoreincorrect " +
                                    " FROM "+schemaName+".groupsessioncontenttest WHERE agendaid = ?  ");
                            stmt.setInt(1,result.getInt(1));
                            resultSet = stmt.executeQuery();
                            while (resultSet.next())
                            {
                                QuestionCollection testCollection = new QuestionCollection();
                                testCollection.id = resultSet.getInt(1);
                                testCollection.agendaId = resultSet.getInt(2);
                                testCollection.contentType = resultSet.getString(3);
                                testCollection.contentSeq = resultSet.getInt(4);
                                testCollection.Instrction = resultSet.getString(5);
                                testCollection.EndNote = resultSet.getString(6);
                                testCollection.IsApplyScoring = resultSet.getBoolean(7);
                                testCollection.scorecorrect = (Integer[]) resultSet.getArray(8).getArray();
                                testCollection.showFeedBack = resultSet.getBoolean(9);
                                testCollection.duration = resultSet.getString(10);
                                testCollection.applyInterval = resultSet.getBoolean(11);
                                testCollection.differentTime = (Integer[]) resultSet.getArray(12).getArray();
                                testCollection.applytimeperquestion = resultSet.getBoolean(13);
                                testCollection.AllowReview = resultSet.getBoolean(14);
                                testCollection.title = resultSet.getString(15);
                                testCollection.description = resultSet.getString(16);
                                testCollection.Description = resultSet.getString(17);
                                testCollection.scoreIncorrect = (Double[]) resultSet.getArray(18).getArray();
                                cycleMeetingAgenda.testSettingList.add(testCollection);
                            }
                    }
                    cycleMeetingAgendas.add(cycleMeetingAgenda);

                }

                if(cycleMeetingAgendas.size() > 0) {
                    for (int i = 0; i < cycleMeetingAgendas.size(); i++) {
                        ContentType contentType = ContentType.valueOf(cycleMeetingAgendas.get(i).contentType);

                            stmt = con
                                    .prepareStatement(
                                            "INSERT INTO "
                                                    + schemaName
                                                    + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                                    + "createdOn,createdBy,updateOn,updatedBy,contenttype,sessionconductor) values (?,?,?,?,?,?,?,?,?,?,CAST(? AS master.contentType),?)",
                                            Statement.RETURN_GENERATED_KEYS);
                            stmt.setInt(1, node.get("cycleMeetingId").asInt());
                            stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                            stmt.setString(3, cycleMeetingAgendas.get(i).sessionName);
                            stmt.setString(4, cycleMeetingAgendas.get(i).sessionDesc);
                            stmt.setTime(5, Time.valueOf(cycleMeetingAgendas.get(i).sessionStartTime));
                            stmt.setTime(6, Time.valueOf(cycleMeetingAgendas.get(i).sessionEndTime));
                            stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                            stmt.setInt(8, loggedInUser.id);
                            stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                            stmt.setInt(10, loggedInUser.id);
                            stmt.setString(11, contentType.name());
                            stmt.setString(12, cycleMeetingAgendas.get(i).sessionConductor);
                            affectedRows = stmt.executeUpdate();

                            if (affectedRows == 0)
                                throw new SQLException("Add Cycle Meeting Failed.");

                            ResultSet generatedKeys = stmt.getGeneratedKeys();

                            if (generatedKeys.next()) {
                                // It gives last inserted Id in divisionId
                                meetingid = generatedKeys.getInt(1);
                                idList.add(meetingid);
                            } else
                                throw new SQLException("No ID obtained");

                        if(cycleMeetingAgendas.get(i).contentList.size() > 0) {
                            for (int j = 0; j < cycleMeetingAgendas.get(i).contentList.size(); j++) {
                                Integer contentIdArr[] = new Integer[cycleMeetingAgendas.get(i).contentList.get(j).contentId.length];
                                stmt = con.prepareStatement("INSERT INTO  "
                                                + schemaName
                                                + ".cyclemeetingsessioncontentinfo (agendaid,contenttype,contentseq,createdon, "
                                                + "createdby,updateon,updatedby,contentid,title,description) "
                                                + "VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?)",
                                        Statement.RETURN_GENERATED_KEYS);

                                stmt.setInt(1, meetingid);
                                stmt.setString(2, cycleMeetingAgendas.get(i).contentList.get(j).contentType);
                                stmt.setInt(3, cycleMeetingAgendas.get(i).contentList.get(j).contentSeq);
                                stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                                stmt.setInt(5, loggedInUser.id);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                for (int k=0;k<cycleMeetingAgendas.get(i).contentList.get(j).contentId.length;k++)
                                {
                                    contentIdArr[k] = cycleMeetingAgendas.get(i).contentList.get(j).contentId[k];
                                }
                                Array arr = con.createArrayOf("int",contentIdArr);
                                stmt.setArray(8, arr);
                                stmt.setString(9,cycleMeetingAgendas.get(i).contentList.get(j).title);
                                stmt.setString(10,cycleMeetingAgendas.get(i).contentList.get(j).description);
                                affectedRows = stmt.executeUpdate();
                            }
                        }

                        if(cycleMeetingAgendas.get(i).questionCollectionList.size() > 0)
                        {
                            for(int j=0;j<cycleMeetingAgendas.get(i).questionCollectionList.size();j++)
                            {
                                Integer[] quesBreak = new Integer[cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionbreakup.length];
                                for (int k=0;k<cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionbreakup.length;k++)
                                {
                                    quesBreak[k] = cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionbreakup[k];
                                }
                                Array breakarr = con.createArrayOf("int",quesBreak);

                                Integer[] questions = new Integer[cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionsId.length];
                                for (int k=0;k<cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionsId.length;k++)
                                {
                                    questions[k] = cycleMeetingAgendas.get(i).questionCollectionList.get(j).questionsId[k];
                                }
                                Array quesarr = con.createArrayOf("int",questions);

                                stmt = con.prepareStatement(" INSERT INTO "+schemaName
                                        +".cyclemeetingsessioncontenttestquestioncollection(agendaid,contenttype,contentseq,createdon," +
                                        " createdby,updateon,updatedby,title,description,questions, randomdelivery, " +
                                        " collectionseq, disregardcomplexitylevel, deliverallquestions, questionbreakup, collectionname)" +
                                        " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                                stmt.setInt(1,meetingid);
                                stmt.setString(2,cycleMeetingAgendas.get(i).questionCollectionList.get(j).contentType);
                                stmt.setInt(3,cycleMeetingAgendas.get(i).questionCollectionList.get(j).contentSeq);
                                stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                                stmt.setInt(5, loggedInUser.id);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setString(8,cycleMeetingAgendas.get(i).questionCollectionList.get(j).title);
                                stmt.setString(9,cycleMeetingAgendas.get(i).questionCollectionList.get(j).description);
                                stmt.setArray(10,quesarr);
                                stmt.setBoolean(11,cycleMeetingAgendas.get(i).questionCollectionList.get(j).randomdelivery);
                                stmt.setInt(12,cycleMeetingAgendas.get(i).questionCollectionList.get(j).collectionseq);
                                stmt.setBoolean(13,cycleMeetingAgendas.get(i).questionCollectionList.get(j).disregardcomplexitylevel);
                                stmt.setBoolean(14,cycleMeetingAgendas.get(i).questionCollectionList.get(j).deliverallquestions);
                                stmt.setArray(15,breakarr);
                                stmt.setString(16,cycleMeetingAgendas.get(i).questionCollectionList.get(j).collection);

                                affectedRows = stmt.executeUpdate();
                            }
                        }

                        if(cycleMeetingAgendas.get(i).testSettingList.size() > 0)
                        {
                            for(int j = 0;j<cycleMeetingAgendas.get(i).testSettingList.size();j++)
                            {
                                Integer[] scoreCorrect = new Integer[cycleMeetingAgendas.get(i).testSettingList.get(j).scorecorrect.length];
                                Integer[] differenttime = new Integer[cycleMeetingAgendas.get(i).testSettingList.get(j).differentTime.length];
                                Double[] scoreIncorrect = new Double[cycleMeetingAgendas.get(i).testSettingList.get(j).scoreIncorrect.length];

                                stmt = con.prepareStatement("INSERT INTO "+schemaName+
                                        " .cyclemeetingsessioncontenttest(agendaid, contenttype, contentseq, createdon, createdby, " +
                                        " updateon, updatedby, testinstruction, testendnote, applyscoring, " +
                                        " scorecorrect, showfeedback, duration, applyinterval, timeperquestion, " +
                                        " applytimeperquestion, allowreview, title, description, testdescription, scoreincorrect)" +
                                        " VALUES (?,CAST(? AS master.contentType),?,?,?,?,?,?,?,?,?,?,CAST (? AS INTERVAL),?,?,?,?,?,?,?,?)");
                                stmt.setInt(1,meetingid);
                                stmt.setString(2,cycleMeetingAgendas.get(i).testSettingList.get(j).contentType);
                                stmt.setInt(3,cycleMeetingAgendas.get(i).testSettingList.get(j).contentSeq);
                                stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
                                stmt.setInt(5, loggedInUser.id);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setString(8,cycleMeetingAgendas.get(i).testSettingList.get(j).Instrction);
                                stmt.setString(9,cycleMeetingAgendas.get(i).testSettingList.get(j).EndNote);
                                stmt.setBoolean(10,cycleMeetingAgendas.get(i).testSettingList.get(j).IsApplyScoring);
                                for(int k =0; k<cycleMeetingAgendas.get(i).testSettingList.get(j).scorecorrect.length;k++)
                                {
                                    scoreCorrect[k] = cycleMeetingAgendas.get(i).testSettingList.get(j).scorecorrect[k];
                                }
                                Array correctarr = con.createArrayOf("int",scoreCorrect);
                                stmt.setArray(11,correctarr);
                                stmt.setBoolean(12,cycleMeetingAgendas.get(i).testSettingList.get(j).showFeedBack);
                                stmt.setObject(13,cycleMeetingAgendas.get(i).testSettingList.get(j).duration);
                                stmt.setBoolean(14,cycleMeetingAgendas.get(i).testSettingList.get(j).applyInterval);
                                for(int k =0; k<cycleMeetingAgendas.get(i).testSettingList.get(j).differentTime.length;k++)
                                {
                                    differenttime[k] = cycleMeetingAgendas.get(i).testSettingList.get(j).differentTime[k];
                                }
                                Array difftimearr = con.createArrayOf("int",differenttime);
                                stmt.setArray(15,difftimearr);
                                stmt.setBoolean(16,cycleMeetingAgendas.get(i).testSettingList.get(j).applytimeperquestion);
                                stmt.setBoolean(17,cycleMeetingAgendas.get(i).testSettingList.get(j).AllowReview);
                                stmt.setString(18,cycleMeetingAgendas.get(i).testSettingList.get(j).title);
                                stmt.setString(19,cycleMeetingAgendas.get(i).testSettingList.get(j).description);
                                stmt.setString(20,cycleMeetingAgendas.get(i).testSettingList.get(j).Description);
                                for(int k =0; k<cycleMeetingAgendas.get(i).testSettingList.get(j).scoreIncorrect.length;k++)
                                {
                                    scoreIncorrect[k] = cycleMeetingAgendas.get(i).testSettingList.get(j).scoreIncorrect[k];
                                }
                                Array incorrectarr = con.createArrayOf("FLOAT8",scoreIncorrect);
                                stmt.setArray(21,incorrectarr);

                                affectedRows = stmt.executeUpdate();
                            }
                        }
                    }
                }
                else
                {
                   idList.add(0);
                    return idList;
                }
                con.commit();
                return idList;
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    if(!con.isClosed())
                     con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                     stmt.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * method to get all cycle meeting agenda
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingAgenda> getAllCycleMeetingAgenda(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet contentResult = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, cyclemeetingid, meetingdate, sessionname, sessiondesc, to_char(sessionstarttime::Time, 'HH12:MI AM')," +
                                    " to_char(sessionendtime::Time, 'HH12:MI AM'), createdon, createdby, updateon, updatedby, contenttype , sessionconductor " +
                                    " FROM " + schemaName + ".cyclemeetingagenda " +
                                    " ORDER BY sessionstarttime ASC");
                    result = stmt.executeQuery();

                    while (result.next()) {

                        CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.cycleMeetingId = result.getInt(2);
                        cycleMeetingAgenda.meetingDate = result.getDate(3);
                        cycleMeetingAgenda.sessionName = result.getString(4);
                        cycleMeetingAgenda.sessionDesc = result.getString(5);
                        cycleMeetingAgenda.sessionStartTime = result.getString(6);
                        cycleMeetingAgenda.sessionEndTime = result.getString(7);
                        cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        cycleMeetingAgenda.createBy = result.getInt(9);
                        cycleMeetingAgenda.updatedOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        cycleMeetingAgenda.updatedBy = result.getInt(11);
                        cycleMeetingAgenda.contentType = result.getString(12);
                        cycleMeetingAgenda.sessionConductor = result.getString(13);
                        cycleMeetingAgenda.contentList = new ArrayList<>();

                        if(result.getString(12).equals("MIXED"))
                        {
                            stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby, " +
                                    " c1.updateon, c1.updatedby , c1.contentid , c1.title , c1. description " +
                                    " FROM "+schemaName+".cyclemeetingsessioncontentinfo as c1 WHERE  c1.agendaid = ?");
                            stmt.setInt(1,result.getInt(1));
                            contentResult = stmt.executeQuery();
                            while (contentResult.next())
                            {
                                Content content = new Content();
                                content.id = contentResult.getInt(1);
                                content.agendaId = contentResult.getInt(2);
                                content.contentType = contentResult.getString(3);
                                content.contentSeq = contentResult.getInt(4);
                                content.createdOn = contentResult.getTimestamp(5);
                                content.createBy = contentResult.getInt(6);
                                content.updateOn = contentResult.getTimestamp(7);
                                content.updateBy = contentResult.getInt(8);
                                content.contentId = (Integer[]) contentResult.getArray(9).getArray();
                                content.title = contentResult.getString(10);
                                content.description = contentResult.getString(11);
                                cycleMeetingAgenda.contentList.add(content);
                            }

                            stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby, " +
                                    " c1.updateon, c1.updatedby , c1.title , c1. description " +
                                    " FROM "+schemaName+".cyclemeetingsessioncontenttest as c1 WHERE  c1.agendaid = ?");
                            stmt.setInt(1,result.getInt(1));
                            contentResult = stmt.executeQuery();
                            while (contentResult.next())
                            {
                                Content content = new Content();
                                content.id = contentResult.getInt(1);
                                content.agendaId = contentResult.getInt(2);
                                content.contentType = contentResult.getString(3);
                                content.contentSeq = contentResult.getInt(4);
                                content.createdOn = contentResult.getTimestamp(5);
                                content.createBy = contentResult.getInt(6);
                                content.updateOn = contentResult.getTimestamp(7);
                                content.updateBy = contentResult.getInt(8);
                                content.title = contentResult.getString(9);
                                content.description = contentResult.getString(10);
                                cycleMeetingAgenda.contentList.add(content);
                            }
                        }

                        cycleMeetingAgendas.add(cycleMeetingAgenda);
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
            return cycleMeetingAgendas;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /**
     * Method allow user to add cycle meeting agenda.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;
            ResultSet resultSet = null;
            ResultSet seqResultSet = null;
            int id;
            int sequenceNo = 0;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM " + schemaName + ".cycleMeetingAgenda " +
                        " where cyclemeetingid = ? AND  meetingdate = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                stmt.setTime(3, Time.valueOf(node.get("sessionStartTime").asText()));
                stmt.setTime(4, Time.valueOf(node.get("sessionStartTime").asText()));
                stmt.setTime(5, Time.valueOf(node.get("sessionEndTime").asText()));
                stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
                resultSet = stmt.executeQuery();
                if (!resultSet.next()) {
                    ContentType contentType = ContentType.valueOf(node.get("contentType").asText());
                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cycleMeetingAgenda(cycleMeetingId,meetingDate,sessionName,sessionDesc,sessionStartTime,sessionEndTime,"
                                            + "createdOn,createdBy,updateOn,updatedBy,contenttype,sessionconductor) values (?,?,?,?,?,?,?,?,?,?,CAST(? AS master.contentType),?)",
                                    Statement.RETURN_GENERATED_KEYS);
                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                    stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                    stmt.setInt(8, loggedInUser.id);
                    stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                    stmt.setInt(10, loggedInUser.id);
                    stmt.setString(11, contentType.name());
                    if (node.has("sessionConductor"))
                        stmt.setString(12, node.get("sessionConductor").asText());
                    else
                        stmt.setString(12, null);
                    result = stmt.executeUpdate();

                    if (result == 0)
                        throw new SQLException("Add Cycle Meeting Agenda Failed.");

                    ResultSet generatedKeys = stmt.getGeneratedKeys();

                    if (generatedKeys.next())
                        // It gives last inserted Id in id
                        id = generatedKeys.getInt(1);
                    else
                        throw new SQLException("No ID obtained");

                    stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".cyclemeetingsessioncontent" +
                            " where agendaid = ? ");
                    stmt.setInt(1, id);
                    seqResultSet = stmt.executeQuery();

                    if (seqResultSet.next()) {
                        if (seqResultSet.getInt("sequenceNo") > 0) {
                            sequenceNo = seqResultSet.getInt("sequenceNo");
                            sequenceNo++;
                        } else {
                            sequenceNo = 0;
                            sequenceNo++;
                        }
                    } else {
                        sequenceNo = 0;
                        sequenceNo++;
                    }


                    Integer[] array = new Integer[]{};
                    Array arr = con.createArrayOf("int",array);

                    if(contentType.name().equals("INFO") || contentType.name().equals("ACTIVITY")) {
                        stmt = con.prepareStatement("INSERT INTO " +
                                schemaName +
                                ".cyclemeetingsessioncontentinfo(title,description,agendaid,contenttype,contentseq," +
                                " createdon,createdby , updateon, updatedby,contentid) " +
                                " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                        stmt.setString(1,null);
                        stmt.setString(2,null);
                        stmt.setInt(3, id);
                        stmt.setString(4, contentType.name());
                        stmt.setInt(5, sequenceNo);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                        stmt.setInt(9, loggedInUser.id);
                        stmt.setArray(10,arr);

                        result = stmt.executeUpdate();
                    }

                    if(contentType.name().equals("TEST"))
                    {
                        // It can create Empty Integer Array
                        Integer[] intTest = new Integer[]{};
                        Array testIntArr = con.createArrayOf("int",intTest);

                        // It can create Empty Double Array
                        Double[] doubleArr = new Double[]{};
                        Array testDoubArr = con.createArrayOf("FLOAT8",doubleArr);

                        stmt = con.prepareStatement(" INSERT INTO " +
                                schemaName +
                                ".cyclemeetingsessioncontenttest(title,description,agendaid,contenttype,contentseq, " +
                                " createdon,createdby,updateon, updatedby,scorecorrect,scoreincorrect,duration,timeperquestion, " +
                                " testinstruction, testendnote, applyscoring,showfeedback,applyinterval," +
                                " applytimeperquestion, allowreview,testdescription ) " +
                                " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?,?,CAST(? AS INTERVAL),?,?,?,?,?,?,?,?,?) ");

                        stmt.setString(1,null);
                        stmt.setString(2,null);
                        stmt.setInt(3, id);
                        stmt.setString(4, contentType.name());
                        stmt.setInt(5, sequenceNo);
                        stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                        stmt.setInt(7, loggedInUser.id);
                        stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                        stmt.setInt(9, loggedInUser.id);
                        stmt.setArray(10,testIntArr);
                        stmt.setArray(11,testDoubArr);
                        stmt.setObject(12,"00:00");
                        stmt.setArray(13,testIntArr);
                        stmt.setString(14,null);
                        stmt.setString(15,null);
                        stmt.setBoolean(16,false);
                        stmt.setBoolean(17,false);
                        stmt.setBoolean(18,false);
                        stmt.setBoolean(19,false);
                        stmt.setBoolean(20,false);
                        stmt.setString(21,null);

                        result = stmt.executeUpdate();
                    }


                    if(contentType.name().equals("MIXED"))
                    {
                        for(int i=0;i<node.withArray("mixedContentType").size();i++)
                        {

                            stmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".cyclemeetingsessioncontent" +
                                    " where agendaid = ? ");
                            stmt.setInt(1, id);
                            seqResultSet = stmt.executeQuery();

                            if (seqResultSet.next()) {
                                if (seqResultSet.getInt("sequenceNo") > 0) {
                                    sequenceNo = seqResultSet.getInt("sequenceNo");
                                    sequenceNo++;
                                } else {
                                    sequenceNo = 0;
                                    sequenceNo++;
                                }
                            } else {
                                sequenceNo = 0;
                                sequenceNo++;
                            }

                            ContentType type = ContentType.valueOf(node.withArray("mixedContentType").get(i).get("contentType").asText());

                            if(type.name().equals("INFO") || type.name().equals("ACTIVITY")) {
                                stmt = con.prepareStatement("INSERT INTO " +
                                        schemaName +
                                        ".cyclemeetingsessioncontentinfo(title,description,agendaid,contenttype,contentseq," +
                                        " createdon,createdby , updateon, updatedby,contentid) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                                stmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());


                                if(node.withArray("mixedContentType").get(i).has("description")) {
                                    stmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    stmt.setString(2,null);

                                stmt.setInt(3, id);
                                stmt.setString(4, type.name());
                                stmt.setInt(5, sequenceNo);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                stmt.setInt(9, loggedInUser.id);
                                stmt.setArray(10,arr);

                                result = stmt.executeUpdate();
                            }

                            if(type.name().equals("TEST"))
                            {
                                // It can create Empty Integer Array
                                Integer[] intTest = new Integer[]{};
                                Array testIntArr = con.createArrayOf("int",intTest);

                                // It can create Empty Double Array
                                Double[] doubleArr = new Double[]{};
                                Array testDoubArr = con.createArrayOf("FLOAT8",doubleArr);

                                stmt = con.prepareStatement(" INSERT INTO " +
                                        schemaName +
                                        ".cyclemeetingsessioncontenttest(title,description,agendaid,contenttype,contentseq, " +
                                        " createdon,createdby,updateon, updatedby,scorecorrect,scoreincorrect,duration,timeperquestion, " +
                                        " testinstruction, testendnote, applyscoring,showfeedback,applyinterval," +
                                        " applytimeperquestion, allowreview,testdescription ) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?,?,CAST(? AS INTERVAL),?,?,?,?,?,?,?,?,?) ");

                                stmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());

                                if(node.withArray("mixedContentType").get(i).has("description")) {
                                    stmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    stmt.setString(2,"");

                                stmt.setInt(3, id);
                                stmt.setString(4, type.name());
                                stmt.setInt(5, sequenceNo);
                                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                stmt.setInt(7, loggedInUser.id);
                                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                stmt.setInt(9, loggedInUser.id);
                                stmt.setArray(10,testIntArr);
                                stmt.setArray(11,testDoubArr);
                                stmt.setObject(12,"00:00");
                                stmt.setArray(13,testIntArr);
                                stmt.setString(14,null);
                                stmt.setString(15,null);
                                stmt.setBoolean(16,false);
                                stmt.setBoolean(17,false);
                                stmt.setBoolean(18,false);
                                stmt.setBoolean(19,false);
                                stmt.setBoolean(20,false);
                                stmt.setString(21,null);

                                result = stmt.executeUpdate();
                            }

                        }
                    }

                    con.commit();
                } else {
                    throw new BadRequestException("");
                }
                return id;

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


    /**
     * Method allow user to update cycle meeting agenda.
     *
     * @param node
     * @param loggedInUser
     * @throws Exception
     */
    public static void updateCycleMeetingAgenda(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            PreparedStatement contentStmt = null;
            ResultSet resultSet;
            ResultSet seqResultSet;
            int cyclemeetingId=0;
            Date meetingDate = null;
            int sequenceNo;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT cyclemeetingid, meetingdate FROM " + schemaName+".cycleMeetingAgenda WHERE id = ?");
                stmt.setInt(1,node.get("id").asInt());
                resultSet = stmt.executeQuery();

                if(resultSet.next())
                {
                    cyclemeetingId = resultSet.getInt(1);
                    meetingDate = resultSet.getDate(2);
                }
                stmt = con.prepareStatement("SELECT sessionstarttime , sessionendtime FROM " + schemaName + ".cycleMeetingAgenda " +
                        " where id != ? AND cyclemeetingid = ? AND  meetingdate = ? AND  " +
                        " ((CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?) OR " +
                        " (CAST(sessionstarttime as time) <= ? and CAST(sessionendtime as time) >= ?))");
                stmt.setInt(1,node.get("id").asInt());
                stmt.setInt(2, cyclemeetingId);
                stmt.setDate(3, (java.sql.Date) meetingDate);
                stmt.setTime(4, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText().trim()));
                stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                stmt.setTime(7, Time.valueOf(node.get("sessionEndTime").asText().trim()));
                resultSet = stmt.executeQuery();

                if(!resultSet.next())
                {
                    ContentType contentType = ContentType.valueOf(node.get("contentType").asText());
                    stmt = con
                            .prepareStatement(
                                    "UPDATE "
                                            + schemaName
                                            + ".cycleMeetingAgenda SET cycleMeetingId =? ,meetingDate=?,sessionName=?,sessionDesc=?,sessionStartTime=?,sessionEndTime=?,"
                                            + "updateOn=?,updatedBy=? , contenttype = CAST(? AS master.contentType) , sessionconductor = ? "
                                            + "where id = ? ");
                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setDate(2, java.sql.Date.valueOf(node.get("meetingDate").asText()));
                    stmt.setString(3, node.get("sessionName").asText());
                    stmt.setString(4, node.get("sessionDesc").asText());
                    stmt.setTime(5, Time.valueOf(node.get("sessionStartTime").asText()));
                    stmt.setTime(6, Time.valueOf(node.get("sessionEndTime").asText()));
                    stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));
                    stmt.setInt(8, loggedInUser.id);
                    stmt.setString(9, contentType.name());
                    stmt.setString(10, node.get("sessionConductor").asText());
                    stmt.setInt(11, node.get("id").asInt());

                    if(contentType.name().equals("MIXED")){

                        contentStmt = con.prepareStatement("DELETE FROM "+schemaName+" " +
                                " .cyclemeetingsessioncontentinfo WHERE agendaid = ? ");
                        contentStmt.setInt(1,node.get("id").asInt());
                        contentStmt.executeUpdate();

                        contentStmt = con.prepareStatement("DELETE FROM "+schemaName+" " +
                                " .cyclemeetingsessioncontenttest WHERE agendaid = ? ");
                        contentStmt.setInt(1,node.get("id").asInt());
                        contentStmt.executeUpdate();


                        for(int i=0;i<node.withArray("mixedContentType").size();i++)
                        {
                            contentStmt = con.prepareStatement("SELECT max(contentseq) as sequenceNo from " + schemaName + ".cyclemeetingsessioncontent" +
                                    " where agendaid = ? ");
                            contentStmt.setInt(1,node.get("id").asInt());
                            seqResultSet = contentStmt.executeQuery();

                            if (seqResultSet.next()) {
                                if (seqResultSet.getInt("sequenceNo") > 0) {
                                    sequenceNo = seqResultSet.getInt("sequenceNo");
                                    sequenceNo++;
                                } else {
                                    sequenceNo = 0;
                                    sequenceNo++;
                                }
                            } else {
                                sequenceNo = 0;
                                sequenceNo++;
                            }

                            Integer[] array = new Integer[]{};
                            Array arr = con.createArrayOf("int",array);

                            ContentType type = ContentType.valueOf(node.withArray("mixedContentType").get(i).get("contentType").asText());

                            if(type.name().equals("INFO") || type.name().equals("ACTIVITY")) {

                                contentStmt = con.prepareStatement("INSERT INTO " +
                                        schemaName +
                                        ".cyclemeetingsessioncontentinfo(title,description,agendaid,contenttype,contentseq," +
                                        " createdon,createdby , updateon, updatedby,contentid) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,?,?)");

                                contentStmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());

                                if(node.withArray("mixedContentType").get(i).has("description")) {
                                    contentStmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    contentStmt.setString(2,"");

                                contentStmt.setInt(3, node.get("id").asInt());
                                contentStmt.setString(4, type.name());
                                contentStmt.setInt(5, sequenceNo);
                                contentStmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(7, loggedInUser.id);
                                contentStmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(9, loggedInUser.id);
                                contentStmt.setArray(10,arr);

                                contentStmt.executeUpdate();
                            }

                            if(type.name().equals("TEST"))
                            {
                                contentStmt = con.prepareStatement("INSERT INTO " +
                                        schemaName +
                                        ".cyclemeetingsessioncontenttest(title,description,agendaid,contenttype,contentseq," +
                                        " createdon,createdby , updateon, updatedby) " +
                                        " VALUES (?,?,?,CAST(? AS master.contentType),?,?,?,?,? )");

                                contentStmt.setString(1,node.withArray("mixedContentType").get(i).get("title").asText());

                                if(node.withArray("mixedContentType").get(i).has("description")) {
                                    contentStmt.setString(2, node.withArray("mixedContentType").get(i).get("description").asText());
                                }
                                else
                                    contentStmt.setString(2,"");

                                contentStmt.setInt(3, node.get("id").asInt());
                                contentStmt.setString(4, type.name());
                                contentStmt.setInt(5, sequenceNo);
                                contentStmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(7, loggedInUser.id);
                                contentStmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                                contentStmt.setInt(9, loggedInUser.id);

                                contentStmt.executeUpdate();
                            }
                        }
                    }

                    stmt.executeUpdate();
                    con.commit();
                }
                else
                {
                    throw new BadRequestException("");
                }


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

    /**
     * Method allow user to delete cycle meeting agenda.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteCycleMeetingAgenda(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cycleMeetingAgenda WHERE id = ?");

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

    /**
     * method used for get agenda by date
     *
     * @param cycleMeetingId
     * @param date
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingAgenda> getAgendaByDate(int cycleMeetingId, Date date, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingAgenda).equals("Write")) {
            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingAgenda> cycleMeetingAgendas = new ArrayList<CycleMeetingAgenda>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet contentResult = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, sessionname, sessiondesc, to_char(sessionstarttime::Time, 'HH12:MI AM')," +
                                    "to_char(sessionendtime::Time, 'HH12:MI AM'),createdon, createdby, updateon, updatedby,contenttype , sessionconductor " +
                                    " FROM " + schemaName + ".cyclemeetingagenda where cyclemeetingid= ? and meetingdate = ? " +
                                    " ORDER BY sessionstarttime ASC");

                    stmt.setInt(1, cycleMeetingId);
                    stmt.setDate(2, new java.sql.Date(date.getTime()));
                    result = stmt.executeQuery();

                    while (result.next()) {

                        CycleMeetingAgenda cycleMeetingAgenda = new CycleMeetingAgenda();
                        cycleMeetingAgenda.id = result.getInt(1);
                        cycleMeetingAgenda.sessionName = result.getString(2);
                        cycleMeetingAgenda.sessionDesc = result.getString(3);
                        cycleMeetingAgenda.sessionStartTime = result.getString(4);
                        cycleMeetingAgenda.sessionEndTime = result.getString(5);
                        cycleMeetingAgenda.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        cycleMeetingAgenda.createBy = result.getInt(7);
                        cycleMeetingAgenda.updatedOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(8).getTime())));
                        cycleMeetingAgenda.updatedBy = result.getInt(9);
                        cycleMeetingAgenda.contentType = result.getString(10);
                        cycleMeetingAgenda.sessionConductor = result.getString(11);
                        cycleMeetingAgenda.meetingDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(date.getTime())));
                        cycleMeetingAgenda.cycleMeetingId = cycleMeetingId;
                        cycleMeetingAgenda.contentList = new ArrayList<>();

                        if(result.getString(10).equals("MIXED"))
                        {
                            stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby, " +
                                    " c1.updateon, c1.updatedby , c1.contentid , c1.title , c1. description " +
                                    " FROM "+schemaName+".cyclemeetingsessioncontentinfo as c1 WHERE  c1.agendaid = ?");
                            stmt.setInt(1,result.getInt(1));
                            contentResult = stmt.executeQuery();
                            while (contentResult.next())
                            {
                                Content content = new Content();
                                content.id = contentResult.getInt(1);
                                content.agendaId = contentResult.getInt(2);
                                content.contentType = contentResult.getString(3);
                                content.contentSeq = contentResult.getInt(4);
                                content.createdOn = contentResult.getTimestamp(5);
                                content.createBy = contentResult.getInt(6);
                                content.updateOn = contentResult.getTimestamp(7);
                                content.updateBy = contentResult.getInt(8);
                                content.contentId = (Integer[]) contentResult.getArray(9).getArray();
                                content.title = contentResult.getString(10);
                                content.description = contentResult.getString(11);
                                cycleMeetingAgenda.contentList.add(content);
                            }

                            stmt = con.prepareStatement("SELECT c1.id, c1.agendaid, c1.contenttype,c1.contentseq, c1.createdon, c1.createdby, " +
                                    " c1.updateon, c1.updatedby , c1.title , c1. description " +
                                    " FROM "+schemaName+".cyclemeetingsessioncontenttest as c1 WHERE  c1.agendaid = ?");
                            stmt.setInt(1,result.getInt(1));
                            contentResult = stmt.executeQuery();
                            while (contentResult.next())
                            {
                                Content content = new Content();
                                content.id = contentResult.getInt(1);
                                content.agendaId = contentResult.getInt(2);
                                content.contentType = contentResult.getString(3);
                                content.contentSeq = contentResult.getInt(4);
                                content.createdOn = contentResult.getTimestamp(5);
                                content.createBy = contentResult.getInt(6);
                                content.updateOn = contentResult.getTimestamp(7);
                                content.updateBy = contentResult.getInt(8);
                                content.title = contentResult.getString(9);
                                content.description = contentResult.getString(10);
                                cycleMeetingAgenda.contentList.add(content);
                            }
                        }

                        cycleMeetingAgendas.add(cycleMeetingAgenda);
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
            return cycleMeetingAgendas;
        } else {
            throw new NotAuthorizedException("");
        }
    }

}

