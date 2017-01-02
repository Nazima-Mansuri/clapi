package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
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

    @JsonProperty("title")
    public String title;

    @JsonProperty("body")
    public String body;

    @JsonProperty("questiontype")
    public String questiontype;

    @JsonProperty("questiontext")
    public String questiontext;

    @JsonProperty("answeroptions")
    public String answeroptions;

    @JsonProperty("answertext")
    public String answertext;

    @JsonProperty("scorecorrect")
    public String scorecorrect;

    @JsonProperty("scoreincorrect")
    public double scoreincorrect;

    @JsonProperty("products")
    public Integer[] products;

    @JsonProperty("keywords")
    public String[] keywords;

    @JsonProperty("createdate")
    public Date createdate;

    @JsonProperty("createby")
    public int createby;

    // make the default constructor visible to package only.
    public Pill() {
    }

    public enum QuestionType {
        MCQ, MRQ, MATRIX, SLIDER, PRIORITY, TEXT, PULLDOWN, NUMBER, LINE, DIVISION, DATE, TIME, EMAIL, URL, NONE;
    }

    public static final int Pill = 18;

    /***
     *
     *
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
                               String products, String keywords, List<String> filePath,LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Pill).equals("Write"))
        {
            String schemaname = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);
                QuestionType quesType;
                if(!questiontype.isEmpty() && questiontype != null) {
                    quesType = QuestionType.valueOf(questiontype.toUpperCase());
                }
                else
                {
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

                stmt = con.prepareStatement(" INSERT INTO "+schemaname
                        +".pills(divid, title, body, questiontype, questiontext, answeroptions," +
                        " answertext, scorecorrect, scoreincorrect, products, keywords, createdate, createby) " +
                        " VALUES (?,?,?,CAST(? AS master.questiontype),?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1,divid);
                stmt.setString(2,title);

                if(!body.isEmpty() && body != null)
                    stmt.setString(3,body);
                else
                    stmt.setString(3,null);

                stmt.setString(4,quesType.name());

                if(!questiontext.isEmpty() && questiontext != null)
                    stmt.setString(5,questiontext);
                else
                    stmt.setString(5,null);

                if(!answeroptions.isEmpty() && answeroptions != null)
                    stmt.setString(6,answeroptions);
                else
                    stmt.setString(6,null);

                if(!answertext.isEmpty() && answertext != null)
                    stmt.setString(7,answertext);
                else
                    stmt.setString(7,null);

                stmt.setInt(8,scorecorrect);

                stmt.setDouble(9,Double.parseDouble(scoreincorrect));

                stmt.setArray(10,prdarr);

                stmt.setArray(11,keyArr);

                stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));

                stmt.setInt(13, loggedInUser.id);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Pills Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int pillId;
                if (generatedKeys.next())
                    // It gives last inserted Id in questionId
                    pillId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                for (int i=0;i<filePath.size();i++)
                {
                    stmt = con.prepareStatement("INSERT INTO "+schemaname
                            +".pillsmedia(pillid, originalmediaurl, resize540xurl, resize250x25uurl," +
                            " videothumbnailurl, createdate, createby) VALUES (?,?,?,?,?,?,?)");

                    stmt.setInt(1,pillId);
                    stmt.setString(2,filePath.get(i));
                    stmt.setString(3,"");
                    stmt.setString(4,"");
                    stmt.setString(5,"");
                    stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                    stmt.setInt(7, loggedInUser.id);

                    stmt.executeUpdate();

                }
                con.commit();
                return pillId;
            }
            finally {
                if(con != null)
                    if(!con.isClosed())
                        con.close();
                if(stmt != null)
                    if(!stmt.isClosed())
                        stmt.close();
            }
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

    public static int deletePills(LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if(Permissions.isAuthorised(userRole,Pill).equals("Write"))
        {
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRow = 0;
            try {
                stmt = con.prepareStatement("");
            }
            finally {

            }
            return affectedRow;
        }
        else
        {
            throw new NotAuthorizedException("");
        }
    }

}
