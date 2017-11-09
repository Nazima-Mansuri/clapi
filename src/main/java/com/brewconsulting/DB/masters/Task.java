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
import java.util.Date;
import java.util.List;

import static com.brewconsulting.DB.utils.stringToDate;

/**
 * Created by lcom53 on 14/10/16.
 */
public class Task {

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("id")
    public int id;

    @JsonView({UserViews.groupTaskView.class})
    @JsonProperty("groupId")
    public int groupId;

    @JsonView({UserViews.childTaskView.class})
    @JsonProperty("cycleMeetingId")
    public int cycleMeetingId;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("title")
    public String title;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("description")
    public String description;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("status")
    public String status;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("dueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date dueDate;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("reminders")
    public Integer[] reminders;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("assignTo")
    public int assignTo;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("assignBy")
    public int assignBy;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("createdOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date createOn;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("createdBy")
    public int createBy;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("updateOn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public Date updateOn;

    @JsonView({UserViews.childTaskView.class, UserViews.groupTaskView.class})
    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("division")
    public int division;

    @JsonProperty("userDetails")
    public ArrayList<UserDetail> userDetails;

    @JsonProperty("remindUserDetails")
    public ArrayList<UserDetail> remindUserDetails;

    public static final int Task = 13;

    public enum taskStatus {
        New, Started, Mid_Way, Almost_Done, Awaiting_Clarification, Done
    }

    public enum taskCategory {
        Prepare_Edit_Content, Venue_Arrangements, Travel_Arrangement, Misc
    }

    // make default constructor to visible package
    public Task() {
    }

    /***
     * Method allows user to get All Details of Group Tasks.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Task> getAllGroupTasks(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Task> groupTasks = new ArrayList<Task>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby, u.username,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone FROM " +
                                    " (SELECT * FROM " + schemaName + ".grouptasks g WHERE groupid = ? AND" +
                                    "  ((task).assignedTo = ? OR (task).assignedBy = ?  OR ? = ANY((task).reminders ::int[])))g" +
                                    " left join master.users u on u.id = (task).assignedto " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = (g.task).assignedto " +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, id);
                    stmt.setInt(2, loggedInUser.id);
                    stmt.setInt(3, loggedInUser.id);
                    stmt.setInt(4, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.groupId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
                        task.updateBy = result.getInt(13);
                        task.userDetails = new ArrayList<>();
                        task.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));

                        groupTasks.add(task);
                    }
                }
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

            return groupTasks;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method allows user to get Details of Particular Group Task.
     *
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static Task getGroupTaskById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            Task groupTask = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(task).title title,(task).description description ," +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby , u.username ,u.firstname,u.lastname,(uf.address).city city," +
                                    " (uf.address).state state, (uf.address).phone phone FROM " +
                                    " (select * from " + schemaName + ".grouptasks g where g.id = ?)g" +
                                    " left join master.users u on u.id = (task).assignedto " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = (task).assignedto ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        groupTask = new Task();
                        groupTask.id = result.getInt(1);
                        groupTask.groupId = result.getInt(2);
                        groupTask.title = result.getString(3);
                        groupTask.description = result.getString(4);
                        groupTask.status = result.getString(5);
                        groupTask.dueDate = result.getTimestamp(5);
                        groupTask.reminders = (Integer[]) result.getArray(7).getArray();
                        groupTask.assignTo = result.getInt(8);
                        groupTask.assignBy = result.getInt(9);
                        groupTask.createOn = result.getTimestamp(10);
                        groupTask.createBy = result.getInt(11);
                        groupTask.updateOn = result.getTimestamp(12);
                        groupTask.updateBy = result.getInt(13);
                        groupTask.userDetails = new ArrayList<>();
                        groupTask.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));
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
            return groupTask;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method used to insert Group Task
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addGroupTask(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            // TODO: set up the reminder int array
            Integer[] remindArr = new Integer[node.withArray("reminders").size()];

            // Convert JsonArray into String Array
            for (int i = 0; i < node.withArray("reminders").size(); i++) {
                remindArr[i] = node.withArray("reminders").get(i).asInt();
            }

            Array remind = con.createArrayOf("int", remindArr);
            try {
                con.setAutoCommit(false);

                taskStatus statusType = taskStatus.valueOf(node.get("status").asText());
                taskCategory categoryType = taskCategory.valueOf(node.get("title").asText());

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".groupTasks(groupId,task,createdon,createdby,updateon,updateby) values " +
                                        "(?,ROW(CAST(? AS master.taskCategory),?,CAST(? AS master.taskStatus),?,?,?,?),?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("groupId").asInt());
                stmt.setString(2, categoryType.name());
                stmt.setString(3, node.get("description").asText());
                stmt.setString(4, statusType.name());
                stmt.setTimestamp(5, new Timestamp((stringToDate(node.get("dueDate").asText())).getTime()));
                stmt.setArray(6, remind);
                stmt.setInt(7, node.get("assignedTo").asInt());
                stmt.setInt(8, loggedInUser.id);
                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                stmt.setInt(10, loggedInUser.id);
                stmt.setTimestamp(11, new Timestamp((new Date()).getTime()));
                stmt.setInt(12, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Group Task Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupTaskId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    groupTaskId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return groupTaskId;
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
     * Method allows user to Update Group Task in Database.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateGroupTask(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            // TODO: set up the reminder int array
            Integer[] remindArr = new Integer[node.withArray("reminders").size()];

            // Convert JsonArray into String Array
            for (int i = 0; i < node.withArray("reminders").size(); i++) {
                remindArr[i] = node.withArray("reminders").get(i).asInt();
            }

            Array remind = con.createArrayOf("int", remindArr);

            try {
                if (con != null) {
                    taskStatus statusType = taskStatus.valueOf(node.get("status").asText());
                    taskCategory categoryType = taskCategory.valueOf(node.get("title").asText());

                    if (node.get("groupId").asInt() > 0) {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".groupTasks SET task = ROW(CAST(? AS master.taskCategory),?,"
                                        + " CAST(? AS master.taskStatus),?,?,?,?) ," +
                                        "  updateOn = ?, updateBy = ?"
                                        + " WHERE id = ?");
                    } else {
                        stmt = con
                                .prepareStatement("UPDATE "
                                        + schemaName
                                        + ".usertasks SET task = ROW(CAST(? AS master.taskCategory),?,"
                                        + " CAST(? AS master.taskStatus),?,?,?,?) ," +
                                        "  updateOn = ?, updateBy = ?"
                                        + " WHERE id = ?");
                    }
                    stmt.setString(1, categoryType.name());
                    stmt.setString(2, node.get("description").asText());
                    stmt.setString(3, statusType.name());
                    stmt.setTimestamp(4, new Timestamp((stringToDate(node.get("dueDate").asText())).getTime()));
                    stmt.setArray(5, remind);
                    stmt.setInt(6, node.get("assignedTo").asInt());
                    stmt.setInt(7, node.get("assignBy").asInt());
                    stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                    stmt.setInt(9, loggedInUser.id);
                    stmt.setInt(10, node.get("id").asInt());

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
     * Method allows user to Delete Group Task from Database.
     *
     * @param loggedInUser
     * @param id
     * @throws Exception
     * @Return
     */

    public static int deleteGroupTask(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {

                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".groupTasks WHERE id = ?");

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

    // ===========================================================================================
    // These all methods for CycleMeetingTask.

    /***
     * Method used to get details of CycleMeeting Tasks.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Task> getCycleMeetingTasks(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Task> groupTasks = new ArrayList<Task>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT c.id,cycleMeetingId,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, c.createdon ," +
                                    " c.createdby, c.updateon, c.updateby , u.username ,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone FROM " +
                                    schemaName + ".cycleMeetingTasks c , master.users u , " + schemaName + ".userprofile uf " +
                                    " WHERE u.id = (task).assignedto AND uf.userid = (task).assignedto AND cycleMeetingId = ? " +
                                    " AND ((task).assignedTo = ? OR ? = ANY((task).reminders ::int[]) OR (task).assignedBy = ?) " +
                                    " ORDER BY c.createdon DESC ");
                    stmt.setInt(1, id);
                    stmt.setInt(2, loggedInUser.id);
                    stmt.setInt(3, loggedInUser.id);
                    stmt.setInt(4, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.cycleMeetingId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
                        task.updateBy = result.getInt(13);
                        task.userDetails = new ArrayList<>();
                        task.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));

                        groupTasks.add(task);
                    }


                }
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

            return groupTasks;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method allows user to get Details of Particular CycleMeeting Task.
     *
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static Task getCycleMeetingTaskById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {

            Task groupTask = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT c.id,cycleMeetingId,(task).title title,(task).description description ," +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedto assignedto , (task).assignedby assignedby, c.createdon ," +
                                    " c.createdby, c.updateon, c.updateby , u.username,u.firstname,u.lastname, (uf.address).city city," +
                                    " (uf.address).state state, (uf.address).phone phone FROM " +
                                    schemaName + ".cycleMeetingTasks c , master.users u , " + schemaName + ".userprofile uf " +
                                    " where u.id = (task).assignedto AND uf.userid = (task).assignedto AND c.id = ? ");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        groupTask = new Task();
                        groupTask.id = result.getInt(1);
                        groupTask.cycleMeetingId = result.getInt(2);
                        groupTask.title = result.getString(3);
                        groupTask.description = result.getString(4);
                        groupTask.status = result.getString(5);
                        groupTask.dueDate = result.getTimestamp(6);
                        groupTask.reminders = (Integer[]) result.getArray(7).getArray();
                        groupTask.assignTo = result.getInt(8);
                        groupTask.assignBy = result.getInt(9);
                        groupTask.createOn = result.getTimestamp(10);
                        groupTask.createBy = result.getInt(11);
                        groupTask.updateOn = result.getTimestamp(12);
                        groupTask.updateBy = result.getInt(13);
                        groupTask.userDetails = new ArrayList<>();
                        groupTask.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));


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
            return groupTask;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method used to insert CycleMeeting task
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addCycleMeetingTask(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            // TODO: set up the reminder int array
            Integer[] remindArr = new Integer[node.withArray("reminders").size()];

            // Convert JsonArray into String Array
            for (int i = 0; i < node.withArray("reminders").size(); i++) {
                remindArr[i] = node.withArray("reminders").get(i).asInt();
            }

            Array remind = con.createArrayOf("int", remindArr);
            try {
                con.setAutoCommit(false);

                taskStatus statusType = taskStatus.valueOf(node.get("status").asText());
                taskCategory categoryType = taskCategory.valueOf(node.get("title").asText());

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cycleMeetingTasks(cycleMeetingId,task,createdon,createdby,updateon,updateby) values " +
                                        "(?,ROW(CAST(? AS master.taskCategory),?,CAST(? AS master.taskStatus),?,?,?,?),?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setInt(1, node.get("cycleMeetingId").asInt());
                stmt.setString(2, categoryType.name());
                stmt.setString(3, node.get("description").asText());
                stmt.setString(4, statusType.name());
                stmt.setTimestamp(5, new Timestamp((stringToDate(node.get("dueDate").asText())).getTime()));
                stmt.setArray(6, remind);
                stmt.setInt(7, node.get("assignedTo").asInt());
                stmt.setInt(8, loggedInUser.id);
                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));
                stmt.setInt(10, loggedInUser.id);
                stmt.setTimestamp(11, new Timestamp((new Date()).getTime()));
                stmt.setInt(12, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Cycle Meeting Task Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int groupTaskId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    groupTaskId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return groupTaskId;
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
     * Method allows user to Update CycleMeeting Task in Database.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateCycleMeetingTask(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            // TODO: set up the reminder int array
            Integer[] remindArr = new Integer[node.withArray("reminders").size()];

            // Convert JsonArray into String Array
            for (int i = 0; i < node.withArray("reminders").size(); i++) {
                remindArr[i] = node.withArray("reminders").get(i).asInt();
            }

            Array remind = con.createArrayOf("int", remindArr);

            try {
                if (con != null) {
                    taskStatus statusType = taskStatus.valueOf(node.get("status").asText());
                    taskCategory categoryType = taskCategory.valueOf(node.get("title").asText());
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".cycleMeetingTasks SET task = ROW(CAST(? AS master.taskCategory),?,"
                                    + " CAST(? AS master.taskStatus),?,?,?,?) ," +
                                    "  updateOn = ?, updateBy = ?"
                                    + " WHERE id = ?");
                    stmt.setString(1, categoryType.name());
                    stmt.setString(2, node.get("description").asText());
                    stmt.setString(3, statusType.name());
                    stmt.setTimestamp(4, new Timestamp((stringToDate(node.get("dueDate").asText())).getTime()));
                    stmt.setArray(5, remind);
                    stmt.setInt(6, node.get("assignedTo").asInt());
                    stmt.setInt(7, node.get("assignBy").asInt());
                    stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                    stmt.setInt(9, loggedInUser.id);
                    stmt.setInt(10, node.get("id").asInt());

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
     * Method allows user to Delete CycleMeeting Task from Database.
     *
     * @param loggedInUser
     * @param id
     * @throws Exception
     * @Return
     */

    public static int deleteCycleMeetingTask(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cycleMeetingTasks WHERE id = ?");

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


    /***
     * It Gives all Group Tasks of Logged in User
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Task> getAllGroupTasksOfLogInUser(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Task> groupTasks = new ArrayList<Task>();
            PreparedStatement stmt = null;
            ResultSet result = null;
            ResultSet userSet = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby, u.username,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone,cg.division FROM " +
                                    " (select * from " + schemaName + ".grouptasks g " +
                                    " where (task).assignedTo = ? OR (task).assignedBy = ? OR ? = ANY((task).reminders ::int[]))g " +
                                    " left join master.users u on u.id = (task).assignedto " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = (g.task).assignedto " +
                                    " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = groupid " +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, loggedInUser.id);
                    stmt.setInt(2, loggedInUser.id);
                    stmt.setInt(3, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.groupId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
//                        task.dueDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.remindUserDetails = new ArrayList<>();

                        if (task.reminders.length > 0) {
                            for (int i = 0; i < task.reminders.length; i++) {
                                stmt = con.prepareStatement(" SELECT username,firstname,lastname FROM master.users WHERE id = ?");
                                stmt.setInt(1, task.reminders[i]);
                                userSet = stmt.executeQuery();
                                while (userSet.next()) {
                                    task.remindUserDetails.add(new UserDetail(task.reminders[i], userSet.getString(1), userSet.getString(2), userSet.getString(3)));
                                }
                            }
                        }

                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
//                        task.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
//                        task.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(12).getTime())));
                        task.updateBy = result.getInt(13);
                        task.userDetails = new ArrayList<>();
                        task.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));
                        task.division = result.getInt(20);
                        groupTasks.add(task);
                    }

                    stmt = con
                            .prepareStatement(" SELECT c.id,cycleMeetingId,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, c.createdon ," +
                                    " c.createdby, c.updateon, c.updateby , u.username ,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone,cg.division FROM " +
                                    schemaName + ".cycleMeetingTasks c " +
                                    " left join master.users u on u.id = (task).assignedto " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = (c.task).assignedto" +
                                    " left join " + schemaName + ".cyclemeeting cc on cc.id = cycleMeetingId " +
                                    " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = cc.groupid " +
                                    " WHERE u.id = (task).assignedto AND uf.userid = (task).assignedto " +
                                    " AND ((task).assignedTo = ? OR ? = ANY((task).reminders ::int[]) OR (task).assignedBy = ?) " +
                                    " ORDER BY c.createdon DESC ");
                    stmt.setInt(1, loggedInUser.id);
                    stmt.setInt(2, loggedInUser.id);
                    stmt.setInt(3, loggedInUser.id);
                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.cycleMeetingId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
//                        task.dueDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.remindUserDetails = new ArrayList<>();

                        if (task.reminders.length > 0) {
                            for (int i = 0; i < task.reminders.length; i++) {
                                stmt = con.prepareStatement(" SELECT username,firstname,lastname FROM master.users WHERE id = ?");
                                stmt.setInt(1, task.reminders[i]);
                                userSet = stmt.executeQuery();
                                while (userSet.next()) {
                                    task.remindUserDetails.add(new UserDetail(task.reminders[i], userSet.getString(1), userSet.getString(2), userSet.getString(3)));
                                }
                            }
                        }

                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
//                        task.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
//                        task.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(12).getTime())));
                        task.updateBy = result.getInt(13);
                        task.userDetails = new ArrayList<>();
                        task.userDetails.add(new UserDetail(result.getInt(8), result.getString(14), result.getString(15), result.getString(16), result.getString(17), result.getString(18), (String[]) result.getArray(19).getArray()));
                        task.division = result.getInt(20);
                        groupTasks.add(task);
                    }

                    stmt = con
                            .prepareStatement(" SELECT g.id,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby, u.username,u.firstname,u.lastname," +
                                    " (uf.address).city city, (uf.address).state state," +
                                    " (uf.address).phone phone FROM " +
                                    " (SELECT * FROM " + schemaName + ".usertasks g WHERE (task).assignedTo = ? " +
                                    " OR (task).assignedBy = ?  OR ? = ANY((task).reminders ::int[]))g" +
                                    " left join master.users u on u.id = (task).assignedto " +
                                    " left join " + schemaName + ".userprofile uf on uf.userid = (g.task).assignedto " +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, loggedInUser.id);
                    stmt.setInt(2, loggedInUser.id);
                    stmt.setInt(3, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.title = result.getString(2);
                        task.description = result.getString(3);
                        task.status = result.getString(4);
                        task.dueDate = result.getTimestamp(5);
//                        task.dueDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(5).getTime())));
                        task.reminders = (Integer[]) result.getArray(6).getArray();
                        task.remindUserDetails = new ArrayList<>();

                        if (task.reminders.length > 0) {
                            for (int i = 0; i < task.reminders.length; i++) {
                                stmt = con.prepareStatement(" SELECT username,firstname,lastname FROM master.users WHERE id = ?");
                                stmt.setInt(1, task.reminders[i]);
                                userSet = stmt.executeQuery();
                                while (userSet.next()) {
                                    task.remindUserDetails.add(new UserDetail(task.reminders[i], userSet.getString(1), userSet.getString(2), userSet.getString(3)));
                                }
                            }
                        }
                        task.assignTo = result.getInt(7);
                        task.assignBy = result.getInt(8);
                        task.createOn = result.getTimestamp(9);
//                        task.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(9).getTime())));
                        task.createBy = result.getInt(10);
                        task.updateOn = result.getTimestamp(11);
//                        task.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(11).getTime())));
                        task.updateBy = result.getInt(12);
                        task.userDetails = new ArrayList<>();
                        task.userDetails.add(new UserDetail(result.getInt(7), result.getString(13), result.getString(14), result.getString(15), result.getString(16), result.getString(17), (String[]) result.getArray(18).getArray()));
                        groupTasks.add(task);
                    }
                }
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

            return groupTasks;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * It Gives all Group Tasks of Logged in User
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Task> getAllTasksOfMonth(int month, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Task> groupTasks = new ArrayList<Task>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement(" SELECT g.id,groupid,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby,cg.division FROM " +
                                    schemaName + ".grouptasks g " +
                                    " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = groupid " +
                                    " where Extract(month from (task).dueDate) = ? AND (task).assignedTo = ? " +
                                    " ORDER BY g.createdon DESC");
                    stmt.setInt(1, month);
                    stmt.setInt(2, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.groupId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
//                        task.dueDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
//                        task.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
//                        task.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(12).getTime())));
                        task.updateBy = result.getInt(13);
                        task.division = result.getInt(14);
                        groupTasks.add(task);
                    }

                    stmt = con
                            .prepareStatement(" SELECT c.id,cycleMeetingId,(task).title title,(task).description description , " +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, c.createdon ," +
                                    " c.createdby, c.updateon, c.updateby ,cg.division FROM " +
                                    schemaName + ".cycleMeetingTasks c " +
                                    " left join " + schemaName + ".cyclemeeting cc on cc.id = cycleMeetingId " +
                                    " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = cc.groupid " +
                                    " WHERE Extract(month from (task).dueDate) = ? AND (task).assignedTo = ? " +
                                    " ORDER BY c.createdon DESC ");
                    stmt.setInt(1, month);
                    stmt.setInt(2, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.cycleMeetingId = result.getInt(2);
                        task.title = result.getString(3);
                        task.description = result.getString(4);
                        task.status = result.getString(5);
                        task.dueDate = result.getTimestamp(6);
//                        task.dueDate = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(6).getTime())));
                        task.reminders = (Integer[]) result.getArray(7).getArray();
                        task.assignTo = result.getInt(8);
                        task.assignBy = result.getInt(9);
                        task.createOn = result.getTimestamp(10);
//                        task.createOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(10).getTime())));
                        task.createBy = result.getInt(11);
                        task.updateOn = result.getTimestamp(12);
//                        task.updateOn = new SimpleDateFormat("dd-MM-yyyy").parse(new SimpleDateFormat("dd-MM-yyyy").format(new java.sql.Date(result.getTimestamp(12).getTime())));
                        task.updateBy = result.getInt(13);
                        task.division = result.getInt(14);
                        groupTasks.add(task);
                    }

                    stmt = con
                            .prepareStatement(" SELECT g.id,(task).title title,(task).description description ," +
                                    " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                                    " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon ," +
                                    " g.createdby, g.updateon, g.updateby FROM " +
                                    schemaName + ".usertasks g " +
                                    " WHERE Extract(month from (task).dueDate) = ? AND (task).assignedTo = ? " +
                                    " ORDER BY g.createdon DESC ");
                    stmt.setInt(1, month);
                    stmt.setInt(2, loggedInUser.id);

                    result = stmt.executeQuery();
                    while (result.next()) {
                        Task task = new Task();
                        task.id = result.getInt(1);
                        task.title = result.getString(2);
                        task.description = result.getString(3);
                        task.status = result.getString(4);
                        task.dueDate = result.getTimestamp(5);
                        task.reminders = (Integer[]) result.getArray(6).getArray();
                        task.assignTo = result.getInt(7);
                        task.assignBy = result.getInt(8);
                        task.createOn = result.getTimestamp(9);
                        task.createBy = result.getInt(10);
                        task.updateOn = result.getTimestamp(11);
                        task.updateBy = result.getInt(12);
                        groupTasks.add(task);
                    }
                }
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

            return groupTasks;
        } else {
            throw new NotAuthorizedException("");
        }
    }


    /***
     * Method used to insert Task in userTasks table.
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addUserTasks(JsonNode node, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            // TODO: set up the reminder int array
            Integer[] remindArr = new Integer[node.withArray("reminders").size()];

            // Convert JsonArray into String Array
            for (int i = 0; i < node.withArray("reminders").size(); i++) {
                remindArr[i] = node.withArray("reminders").get(i).asInt();
            }

            Array remind = con.createArrayOf("int", remindArr);
            try {
                con.setAutoCommit(false);

                taskStatus statusType = taskStatus.valueOf(node.get("status").asText());
                taskCategory categoryType = taskCategory.valueOf(node.get("title").asText());

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".usertasks(task,createdon,createdby,updateon,updateby) values " +
                                        "(ROW(CAST(? AS master.taskCategory),?,CAST(? AS master.taskStatus),?,?,?,?),?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, categoryType.name());

                if (!node.get("description").asText().equalsIgnoreCase("null") && node.get("description") != null)
                    stmt.setString(2, node.get("description").asText());
                else
                    stmt.setString(2, null);

                stmt.setString(3, statusType.name());
                stmt.setTimestamp(4, new Timestamp((stringToDate(node.get("dueDate").asText())).getTime()));
                stmt.setArray(5, remind);
                stmt.setInt(6, node.get("assignedTo").asInt());
                stmt.setInt(7, loggedInUser.id);
                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                stmt.setInt(9, loggedInUser.id);
                stmt.setTimestamp(10, new Timestamp((new Date()).getTime()));
                stmt.setInt(11, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add User Task Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int userTaskId;
                if (generatedKeys.next())
                    // It gives last inserted Id in groupTaskId
                    userTaskId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return userTaskId;
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
     * Method is used to filter tasks according to filter options.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Task> filterTasks(int assignTo, int day, int createBy, LoggedInUser loggedInUser) throws Exception {
        int userRole = loggedInUser.roles.get(0).roleId;
        if (Permissions.isAuthorised(userRole, Task).equals("Read") ||
                Permissions.isAuthorised(userRole, Task).equals("Write")) {
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;
            List<Task> taskList = new ArrayList<>();

            try {

                stmt = con.prepareStatement(" SELECT g.id,groupid,(task).title title,(task).description description ," +
                        " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                        " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon , " +
                        " g.createdby, g.updateon, g.updateby,cg.division,u.username,u.firstname,u.lastname," +
                        " (uf.address).city city, (uf.address).state state,(uf.address).phone phone  " +
                        " FROM (SELECT * FROM " + schemaName + ".grouptasks g " +
                        " WHERE (task).assignedTo = ? OR (task).dueDate::date = current_date::date -  ?::interval " +
                        " OR ((task).dueDate::date < current_date::date - '1 day'::interval AND (task).status != 'Done') " +
                        " OR g.createdby = ?)g " +
                        " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = groupid " +
                        " left join master.users u on u.id = (task).assignedto " +
                        " left join " + schemaName + ".userprofile uf on uf.userid = (g.task).assignedto " +
                        " ORDER BY g.createdon DESC ");

                stmt.setInt(1, assignTo);
                stmt.setString(2, day + " day");
                stmt.setInt(3, createBy);

                result = stmt.executeQuery();

                while (result.next()) {
                    Task task = new Task();
                    task.id = result.getInt(1);
                    task.groupId = result.getInt(2);
                    task.title = result.getString(3);
                    task.description = result.getString(4);
                    task.status = result.getString(5);
                    task.dueDate = result.getTimestamp(6);
                    task.reminders = (Integer[]) result.getArray(7).getArray();
                    task.assignTo = result.getInt(8);
                    task.assignBy = result.getInt(9);
                    task.createOn = result.getTimestamp(10);
                    task.createBy = result.getInt(11);
                    task.updateOn = result.getTimestamp(12);
                    task.updateBy = result.getInt(13);
                    task.division = result.getInt(14);
                    task.userDetails = new ArrayList<>();
                    task.userDetails.add(new UserDetail(result.getInt(8), result.getString(15), result.getString(16), result.getString(17), result.getString(18), result.getString(19), (String[]) result.getArray(20).getArray()));
                    taskList.add(task);
                }

                stmt = con.prepareStatement(" SELECT g.id,cycleMeetingId,(task).title title,(task).description description ," +
                        " (task).status status, (task).dueDate dueDate, (task).reminders reminders, " +
                        " (task).assignedTo assignedTo , (task).assignedBy assignedBy, g.createdon , " +
                        " g.createdby, g.updateon, g.updateby,cg.division, u.username,u.firstname,u.lastname," +
                        " (uf.address).city city, (uf.address).state state,(uf.address).phone phone " +
                        " FROM (SELECT * FROM " + schemaName + ".cycleMeetingTasks g " +
                        " WHERE (task).assignedTo = ? OR (task).dueDate::date = current_date::date -  ?::interval" +
                        " OR ((task).dueDate::date < current_date::date - '1 day'::interval AND (task).status != 'Done')" +
                        " OR g.createdby = ?)g " +
                        " left join " + schemaName + ".cyclemeeting cc on cc.id = cycleMeetingId " +
                        " left join " + schemaName + ".cyclemeetinggroup cg on cg.id = cc.groupid " +
                        " left join master.users u on u.id = (task).assignedto " +
                        " left join " + schemaName + ".userprofile uf on uf.userid = (g.task).assignedto " +
                        " ORDER BY g.createdon DESC ");

                stmt.setInt(1, assignTo);
                stmt.setString(2, day + " day");
                stmt.setInt(3, createBy);

                result = stmt.executeQuery();

                while (result.next()) {
                    Task task = new Task();
                    task.id = result.getInt(1);
                    task.cycleMeetingId = result.getInt(2);
                    task.title = result.getString(3);
                    task.description = result.getString(4);
                    task.status = result.getString(5);
                    task.dueDate = result.getTimestamp(6);
                    task.reminders = (Integer[]) result.getArray(7).getArray();
                    task.assignTo = result.getInt(8);
                    task.assignBy = result.getInt(9);
                    task.createOn = result.getTimestamp(10);
                    task.createBy = result.getInt(11);
                    task.updateOn = result.getTimestamp(12);
                    task.updateBy = result.getInt(13);
                    task.division = result.getInt(14);
                    task.userDetails = new ArrayList<>();
                    task.userDetails.add(new UserDetail(result.getInt(8), result.getString(15), result.getString(16), result.getString(17), result.getString(18), result.getString(19), (String[]) result.getArray(20).getArray()));
                    taskList.add(task);
                }

                stmt = con.prepareStatement(" SELECT g.id,(task).title title,(task).description description ," +
                        "(task).status status, (task).dueDate dueDate, (task).reminders reminders," +
                        " (task).assignedTo assignedTo , (task).assignedBy assignedBy," +
                        " g.createdon, g.createdby, g.updateon, g.updateby,u.username,u.firstname,u.lastname," +
                        " (uf.address).city city, (uf.address).state state,(uf.address).phone phone  " +
                        " FROM (SELECT * FROM " + schemaName + ".usertasks g WHERE (task).assignedTo = ? " +
                        " OR (task).dueDate::date = current_date::date -  ?::interval " +
                        " OR ((task).dueDate::date < current_date::date - '1 day'::interval AND (task).status != 'Done')" +
                        " OR createdby = ?)g " +
                        " left join master.users u on u.id = (task).assignedTo " +
                        " left join " + schemaName + ".userprofile uf on uf.userid = (task).assignedTo " +
                        " ORDER BY createdon DESC ");

                stmt.setInt(1, assignTo);
                stmt.setString(2, day + " day");
                stmt.setInt(3, createBy);

                result = stmt.executeQuery();

                while (result.next()) {
                    Task task = new Task();
                    task.id = result.getInt(1);
                    task.title = result.getString(2);
                    task.description = result.getString(3);
                    task.status = result.getString(4);
                    task.dueDate = result.getTimestamp(5);
                    task.reminders = (Integer[]) result.getArray(6).getArray();
                    task.assignTo = result.getInt(7);
                    task.assignBy = result.getInt(8);
                    task.createOn = result.getTimestamp(9);
                    task.createBy = result.getInt(10);
                    task.updateOn = result.getTimestamp(11);
                    task.updateBy = result.getInt(12);
                    task.userDetails = new ArrayList<>();
                    task.userDetails.add(new UserDetail(result.getInt(7), result.getString(13), result.getString(14), result.getString(15), result.getString(16), result.getString(17), (String[]) result.getArray(18).getArray()));
                    taskList.add(task);
                }
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
            return taskList;
        } else {
            throw new NotAuthorizedException("");
        }
    }
}
