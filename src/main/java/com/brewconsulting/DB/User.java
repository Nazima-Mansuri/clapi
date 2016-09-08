
package com.brewconsulting.DB;

import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.ws.rs.NotAuthorizedException;

public class User {

	@JsonProperty("clientId")
	public int clientId; // -1 means no client

	@JsonProperty("id")
	public int id;

	@JsonProperty("username")
	public String username;

	@JsonProperty("schemaName")
	public String schemaName;

	@JsonProperty("firstName")
	public String firstName;

	@JsonProperty("lastName")
	public String lastName;

	@JsonProperty("roles")
	public ArrayList<Irole> roles;

	public interface Irole {
	}

	// make the constructor private.
	protected User() {

	}

	public static User authenticate(String username, String password) throws ClassNotFoundException, SQLException {
		User user = null;
		Connection con = DBConnectionProvider.getConn();
		try {

			PreparedStatement stmt = con.prepareStatement(
					"select a.id, a.clientId, a.firstName, a.lastName, schemaName, d.id roleid, d.name rolename, a.username "
							+ " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
							+ " where a.isActive and a.username = ? and a.password = ? and a.clientId = b.id and "
							+ " a.id = c.userId and c.roleId = d.id");
			stmt.setString(1, username);
			stmt.setString(2, password);

			final ResultSet masterUsers = stmt.executeQuery();
			while (masterUsers.next()) {
				if (user == null) { // execute for the first iteration
					user = new User();
					user.id = masterUsers.getInt(1);
					user.clientId = masterUsers.getInt(2);
					user.firstName = masterUsers.getString(3);
					user.lastName = masterUsers.getString(4);
					user.schemaName = masterUsers.getString(5);
					user.username = masterUsers.getString(8);
					user.roles = new ArrayList<Irole>();
					user.roles.add(new Irole() {
						public int roleid = masterUsers.getInt(6);
						public String rolename = masterUsers.getString(7);
					});
					continue;
				}

				if (user.roles != null) {
					user.roles.add(new Irole() {
						public int roleid = masterUsers.getInt(6);
						public String rolename = masterUsers.getString(7);
					});
				}
			}
		} finally {
			if (con != null)
				con.close();
		}

		return user;
	}

	/***
	 * Get the basic details of the user.
	 * 
	 * @param id
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static User find(Integer id) throws ClassNotFoundException, SQLException {
		Connection con = DBConnectionProvider.getConn();
		User user = null;
		try {

			PreparedStatement stmt = con.prepareStatement(
					"select a.id, a.clientId, a.firstname, a.lastname, schemaName, d.id roleid, d.name rolename, a.username "
							+ " from master.users a, master.clients b, master.userRoleMap c, master.roles d "
							+ " where a.isActive and a.id = ? and a.clientId = b.id and "
							+ " a.id = c.userId and c.roleId = d.id");
			stmt.setInt(1, id);
			final ResultSet masterUsers = stmt.executeQuery();
			while (masterUsers.next()) {
				if (user == null) { // execute for the first iteration
					user = new User();
					user.id = id;
					user.clientId = masterUsers.getInt(2);
					user.schemaName = masterUsers.getString(5);
					user.firstName = masterUsers.getString(3);
					user.lastName = masterUsers.getString(4);
					user.username = masterUsers.getString(8);
					user.roles = new ArrayList<Irole>();
					user.roles.add(new Irole() {
						public int id = masterUsers.getInt(6);
						public String name = masterUsers.getString(7);
					});

					continue;
				}

				if (user.roles != null) {
					user.roles.add(new Irole() {
						public int id = masterUsers.getInt(6);
						public String name = masterUsers.getString(7);
					});
				}
			}
		} finally {
			if (con != null)
				con.close();
		}
		return user;

	}

	/***
	 * User profile class inherits User and includes profile data that comes
	 * from client db schema.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static UserProfile getProfile(JsonNode loggedInUser, int id) throws Exception {
		// check if the profile request of for self? else check if the role
		// allows it.
		JsonNode rolesNode = loggedInUser.get("roles");
		Iterator<JsonNode> it = rolesNode.elements();
		UserProfile user = null;

		// check if the user has permission to see others profile.
		if (loggedInUser.get("id").asInt() != id) {
			boolean isAllowed = false;

			while (it.hasNext()) {
				if (Permissions.isAuthorised(it.next().get("roleid").asInt(), Permissions.USER_PROFILE,
						Permissions.READ_ONLY)) {
					isAllowed = true;
					break;
				}
			}

			if (!isAllowed)
				throw new NotAuthorizedException("");
			
			user = new UserProfile(find(id));
		} else {
			user = new UserProfile();
			user.id = loggedInUser.get("id").asInt();
			user.clientId = loggedInUser.get("clientId").asInt();
			user.schemaName = loggedInUser.get("schemaName").asText();
			user.firstName = loggedInUser.get("firstName").asText();
			user.lastName = loggedInUser.get("lastName").asText();
			user.username = loggedInUser.get("username").asText();
			user.roles = new ArrayList<Irole>();
			while (it.hasNext()) {
				final JsonNode roleNode = it.next();
				user.roles.add(new Irole() {
					public int id = roleNode.get("roleid").asInt();
					public String name = roleNode.get("rolename").asText();
				});
			}
		} // else

		// admin users will not have their profiles filled with any other
		// details as they are not in
		// any firm DB.
		if (!isUserAdmin(user))
			fillProfileInfo(user);

		return user;
	}

	private static void fillProfileInfo(UserProfile user) throws ClassNotFoundException, SQLException {
		Connection con = DBConnectionProvider.getConn();
		System.out.println("Schema"+user.schemaName);
		try {
			PreparedStatement stmt = con.prepareStatement(
					"select (address).addLine1 line1, (address).addLine2 line2, (address).addLine3 line3, "
							+ "(address).city city, (address).state, (address).phone phones, designation, a.divId divid, "
							+ "empNumber, b.name divname," + "a.createDate cdate, a.createBy cby, "
							+ "a.updateDate  udate,  a.updateBy uby from " + user.schemaName + ".userProfile a, "
							+ user.schemaName + ".divisions b where userId = ? and " + "a.divId = b.Id");
			stmt.setInt(1, user.id);

			final ResultSet schemaUsers = stmt.executeQuery();
			if (schemaUsers != null) {
				schemaUsers.next();
				user.addLine1 = schemaUsers.getString("line1");
				user.addLine2 = schemaUsers.getString("line2");
				user.addLine3 = schemaUsers.getString("line3");
				user.city = schemaUsers.getString("city");
				user.state = schemaUsers.getString("state");
				user.phones = (String[]) schemaUsers.getArray("phones").getArray();
				user.designation = schemaUsers.getString("designation");
				user.divId = schemaUsers.getInt("divid");
				user.divName = schemaUsers.getString("divname");
				user.empNum = schemaUsers.getString("empnumber");
				user.createDate = schemaUsers.getDate("cdate");
				user.createBy = schemaUsers.getInt("cby");
				user.updateDate = schemaUsers.getDate("cdate");
				user.updateBy = schemaUsers.getInt("cby");
			}

		} finally {
			if (con != null)
				con.close();
		}

	}

	private static boolean isUserAdmin(UserProfile user)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (Irole irole : user.roles)
			if (irole.getClass().getDeclaredField("id").getInt(irole) == Permissions.ROLE_ROOT)
				return true;

		return false;
	}

}

