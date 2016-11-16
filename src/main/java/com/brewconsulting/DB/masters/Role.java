package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.NotAuthorizedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Role {
	@JsonProperty("roleid")
	public int roleId;

	@JsonProperty("rolename")
	public String roleName;
	
	public Role(){
		
	}
	public Role (int id, String name){
		this.roleId = id;
		this.roleName = name;
	}

public static final int Role = 14;
	/***
	 *  Method used to give all roles from database.
	 *
	 * @param loggedInUser
	 * @return
	 * @throws Exception
     */
	public static List<Role> getAllRoles(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		int userRole = loggedInUser.roles.get(0).roleId;

		if (Permissions.isAuthorised(userRole, Role).equals("Read") ||
				Permissions.isAuthorised(userRole, Role).equals("Write")) {

			String schemaName = loggedInUser.schemaName;

			Connection con = DBConnectionProvider.getConn();
			ArrayList<Role> roles = new ArrayList<Role>();
			PreparedStatement stmt = null;
			ResultSet result = null;

			try {
				if (con != null) {
					stmt = con
							.prepareStatement("SELECT id, name FROM master.roles");
					result = stmt.executeQuery();
					System.out.print(result);
					while (result.next()) {
						Role role = new Role();
						role.roleId = result.getInt(1);
						role.roleName = result.getString(2);
						roles.add(role);
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
			return roles;
		} else {
			throw new NotAuthorizedException("");
		}
	}
}
