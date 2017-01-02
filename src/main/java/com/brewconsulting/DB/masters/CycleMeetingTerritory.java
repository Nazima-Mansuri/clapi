package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lcom53 on 3/11/16.
 */
public class CycleMeetingTerritory {


    @JsonProperty("id")
    public int id;

    @JsonProperty("cyclemeetingId")
    public int cyclemeetingId;

    @JsonProperty("territoryId")
    public int territoryId;

    @JsonProperty("terrName")
    public String terrName;

    @JsonProperty("username")
    public String username;

    @JsonProperty("fullname")
    public String fullname;

    @JsonProperty("addLine1")
    public String addLine1;

    @JsonProperty("addLine2")
    public String addLine2;

    @JsonProperty("addLine3")
    public String addLine3;

    @JsonProperty("city")
    public String city;

    @JsonProperty("state")
    public String state;

    @JsonProperty("phones")
    public String[] phones;

    @JsonProperty("profileImage")
    public String profileImage;

    public static final int CycleMeetingTerritory=8;
    /***
     * Method used to give all cyclemeeting and territory details.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeetingTerritory> getAllCycleMeetingTerr(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Read") ||
                Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeetingTerritory> cycleMeetingTerritories = new ArrayList<CycleMeetingTerritory>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT c.id, cyclemeetingid, territoryid ,t.name ,u.username,u.firstname,u.lastname, " +
                                    " (uf.address).addLine1 addLine1,(uf.address).addLine2 addLine2," +
                                    " (uf.address).addLine3 addLine3,(uf.address).city city,(uf.address).state state," +
                                    " (uf.address).phone phones , uf.profileimage " +
                                    " FROM " + schemaName + ".cyclemeetingterritories c " +
                                    " left join " + schemaName + ".territories t on c.territoryid = t.id" +
                                    " left join " + schemaName + ".userterritorymap t1 on territoryid = t1.terrid" +
                                    " left join master.users u on t1.userid = u.id" +
                                    " left join " + schemaName + ".userprofile uf on t1.userid = uf.userid " +
                                    "where c.cyclemeetingid = ? ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        CycleMeetingTerritory meetingTerritory = new CycleMeetingTerritory();
                        meetingTerritory.id = result.getInt(1);
                        meetingTerritory.cyclemeetingId = result.getInt(2);
                        meetingTerritory.territoryId = result.getInt(3);
                        meetingTerritory.terrName = result.getString(4);
                        meetingTerritory.username = result.getString(5);
                        if(result.getString(6) != null && result.getString(7) != null)
                            meetingTerritory.fullname = result.getString(6) + " " + result.getString(7);
                        else if(result.getString(6) != null && result.getString(7) == null)
                            meetingTerritory.fullname = result.getString(6) + " ";
                        else if(result.getString(6) == null && result.getString(7) != null)
                            meetingTerritory.fullname = result.getString(7);
                        else
                            meetingTerritory.fullname = "";
                        meetingTerritory.addLine1 = result.getString(8);
                        meetingTerritory.addLine2 = result.getString(9);
                        meetingTerritory.addLine3 = result.getString(10);
                        meetingTerritory.city = result.getString(11);
                        meetingTerritory.state = result.getString(12);
                        if (result.getArray(13) != null)
                            meetingTerritory.phones = (String[]) result.getArray(13)
                                    .getArray();
                        meetingTerritory.profileImage = result.getString(14);
                        cycleMeetingTerritories.add(meetingTerritory);
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
            return cycleMeetingTerritories;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     *  Insert Cyclemeeting Territory in database
     *  if Cyclemeeting id already exist then delete that records first
     *  after insert new Cyclemeeting Territory record.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingTerr(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, CycleMeetingTerritory).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int affectedRows = 0;
            int count = 0;
            ResultSet result;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("SELECT id from " + schemaName + ".cyclemeetingterritories WHERE cyclemeetingid = ?");
                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                result = stmt.executeQuery();
                if (result.next()) {
                    System.out.println("If called..");
                    stmt = con.prepareStatement(
                            "DELETE FROM " + schemaName + ".cyclemeetingterritories WHERE cyclemeetingid = ?");

                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    affectedRows = stmt.executeUpdate();
                }

                for (int i = 0; i < node.withArray("territoryId").size(); i++) {

                    stmt = con
                            .prepareStatement(
                                    "INSERT INTO "
                                            + schemaName
                                            + ".cyclemeetingterritories(cycleMeetingId,territoryId) values (?,?)");

                    stmt.setInt(1, node.get("cycleMeetingId").asInt());
                    stmt.setInt(2, node.withArray("territoryId").get(i).asInt());
                    affectedRows = stmt.executeUpdate();
                    count++;
                }

                if (affectedRows == 0)
                    throw new SQLException("Add CycleMeetingTerritory Failed.");

                con.commit();
                return count;

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
}
