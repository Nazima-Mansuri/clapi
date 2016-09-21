package com.brewconsulting.DB.masters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeAssociateUser {
	@JsonProperty("userid")
	public int userid;

	@JsonProperty("username")
	public String username;
	
	/***
	 * Method allows user to get list of username which are not associate to any Territory.
	 * 
	 * @param loggedInUser
	 * @throws Exception
	 * @Return
	 */
	public static List<DeAssociateUser> getDeassociateUser(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;
		ArrayList<DeAssociateUser> userList = new ArrayList<DeAssociateUser>();

		try {
			stmt = con.prepareStatement("select id,username from master.users "
					+ "WHERE id not in(select userid from " + schemaName
					+ ".userterritorymap)");
			result = stmt.executeQuery();
			while (result.next()) {
				DeAssociateUser user = new DeAssociateUser();
				user.userid = result.getInt(1);
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
	}
}
