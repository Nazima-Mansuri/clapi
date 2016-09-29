package com.brewconsulting.DB;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.ws.rs.NotAuthorizedException;

import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.RequiredDataMissing;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class UserProfile extends User {

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

	@JsonProperty("designation")
	public String designation;

	@JsonProperty("divId")
	public int divId;

	@JsonProperty("divName")
	public String divName;

	@JsonProperty("empNum")
	public String empNum;

	@JsonProperty("phones")
	public String[] phones;

	@JsonProperty("createDate")
	public Date createDate;

	@JsonProperty("createBy")
	public int createBy;

	@JsonProperty("updateDate")
	public Date updateDate;

	@JsonProperty("updateBy")
	public int updateBy;

	UserProfile() {

	}

	UserProfile(User u) throws Exception {
		if (u == null)
			throw new Exception("User is null");
		super.id = u.id;
		super.clientId = u.clientId;
		super.roles = u.roles;
		super.schemaName = u.schemaName;
		super.firstName = u.firstName;
		super.lastName = u.lastName;
		super.username = u.username;
	}

	UserProfile(int id) {
		super.id = id;
	}

	public static int createUser(JsonNode node, JsonNode loggedInUser)
			throws ClassNotFoundException, SQLException, RequiredDataMissing, NamingException {

		// TODO: check if the user has rights to perform this action.
		JsonNode userRolesNode = loggedInUser.get("roles");
		Iterator<JsonNode> iterator = userRolesNode.elements();

		boolean isAllowed = false;

		while (iterator.hasNext()) {
			if (Permissions.isAuthorised(iterator.next().get("roleid").asInt(),
					Permissions.USER_PROFILE, Permissions.READ_ONLY)) {

				isAllowed = true;
				break;
			}
		}

		if (!isAllowed)
			throw new NotAuthorizedException("");
		else {

			Connection con = DBConnectionProvider.getConn();
			try {
				con.setAutoCommit(false);
				PreparedStatement stmt = con
						.prepareStatement(
								"insert into master.users (firstname, lastname, "
										+ "clientId, username, password) values (?,?,?,?,?)",
								Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, node.get("firstName").asText());
				stmt.setString(2, node.get("lastName").asText());
				stmt.setInt(3, node.get("clientId").asInt());
				stmt.setString(4, node.get("username").asText());
				stmt.setString(5, node.get("password").asText());

				int affectedRows = stmt.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Create user failed.");

				ResultSet generatedKeys = stmt.getGeneratedKeys();
				int userid;
				if (generatedKeys.next())
					userid = generatedKeys.getInt(1);
				else
					throw new SQLException("Create user failed. No ID obtained");

				// TODO: set up the phones string array
				String[] arr = { "90909 98989", "78787 78786" };
				Array pharr = con.createArrayOf("text", arr);

				stmt = con
						.prepareStatement("insert into "
								+ loggedInUser.get("schemaName").asText()
								+ ".userProfile(userId,"
								+ "address, designation, divId, empNumber, createBy, updateDate, updateBy) values (?, "
								+ "ROW(?,?,?,?,?,?), ?, ?, ?, ?,?,?)");
				stmt.setInt(1, userid);
				stmt.setString(2, node.get("addLine1").asText());
				stmt.setString(3, node.get("addLine2").asText());
				stmt.setString(4, node.get("addLine3").asText());
				stmt.setString(5, node.get("city").asText());
				stmt.setString(6, node.get("state").asText());
				stmt.setArray(7, pharr);
				stmt.setString(8, node.get("designation").asText());
				stmt.setInt(9, node.get("divId").asInt());
				stmt.setString(10, node.get("empNumber").asText());
				stmt.setInt(11, node.get("createBy").asInt());
				stmt.setTimestamp(12, new Timestamp((new Date()).getTime()));
				stmt.setInt(13, node.get("updateBy").asInt());

				stmt.executeUpdate();

				if (node.has("roles")) {
					JsonNode rolesNode = node.get("roles");
					Iterator<JsonNode> it = rolesNode.elements();

					while (it.hasNext()) {
						JsonNode role = it.next();
						stmt = con
								.prepareStatement("insert into master.userRoleMap (userId, roleId, effectDate,createBy) values"
										+ "(?,?,?,?)");
						stmt.setInt(1, userid);
						stmt.setInt(2, role.get("roleid").asInt());
						stmt.setTimestamp(3,
								new Timestamp((new Date()).getTime()));
						stmt.setInt(4, 1);
						stmt.executeUpdate();
					}
				} else
					throw new RequiredDataMissing("Role is required");
				con.commit();
				return userid;
			} catch (Exception ex) {
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

	/***
	 * Method allows user to get list of username which are not associate to any
	 * Territory.
	 * 
	 * @param loggedInUser
	 * @throws Exception
	 * @Return
	 */
	public static List<UserProfile> getDeassociateUser(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		int userRole = loggedInUser.roles.get(0).roleId;

		if (Permissions.isAuthorised(userRole, Permissions.USER_PROFILE,
				Permissions.getAccessLevel(userRole))) {

			String schemaName = loggedInUser.schemaName;
			Connection con = DBConnectionProvider.getConn();
			PreparedStatement stmt = null;
			ResultSet result = null;
			ArrayList<UserProfile> userList = new ArrayList<UserProfile>();

			try {

				stmt = con.prepareStatement("select u.id,u.username,"
						+ "(up.address).city city from master.users u "
						+ "join client1.userprofile up on up.userid=u.id "
						+ "WHERE u.id not in(select userid from " + schemaName
						+ ".userterritorymap)");

				result = stmt.executeQuery();
				while (result.next()) {
					UserProfile user = new UserProfile();
					user.id = result.getInt(1);
					user.username = result.getString(2);
					user.city = result.getString(3);
					userList.add(user);
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
			return userList;
		} else {
			throw new NotAuthorizedException("");
		}
	}
	
	/**
	 * Method allows user to get list of all User.
	 * 
	 * @param loggedInUser
	 * @return
	 * @throws Exception
	 */
	public static List<UserProfile> getAllUsers(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		int userRole = loggedInUser.roles.get(0).roleId;

		if (Permissions.isAuthorised(userRole, Permissions.USER_PROFILE,
				Permissions.getAccessLevel(userRole))) {
			String schemaName = loggedInUser.schemaName;
			Connection con = DBConnectionProvider.getConn();
			PreparedStatement stmt = null;
			ResultSet result = null;
			ArrayList<UserProfile> userList = new ArrayList<UserProfile>();

			try {
				stmt = con
						.prepareStatement("select id,username from master.users ");
				result = stmt.executeQuery();
				while (result.next()) {
					UserProfile user = new UserProfile();
					user.id = result.getInt(1);
					user.username = result.getString(2);
					userList.add(user);
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
			return userList;
		} else {
			throw new NotAuthorizedException("");
		}
	}
}
