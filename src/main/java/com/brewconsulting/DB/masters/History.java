package com.brewconsulting.DB.masters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class History {

	@JsonProperty("userid")
	public int userid;

	@JsonProperty("username")
	public String username;

	@JsonProperty("firstname")
	public String firstname;

	@JsonProperty("lastname")
	public String lastname;

	@JsonProperty("effectDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	public Date effectDate;

	@JsonProperty("endDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	public Date endDate;

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

	@JsonProperty("empnumber")
	public String empnumber;

	@JsonProperty("isHistory")
	public Boolean isHistory;

	/***
	 * Method allows user to get history of territory.
	 * 
	 * @param loggedInUser
	 * @throws Exception
	 * @Return
	 */
	public static List<History> getAllHistory(int id,LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		

		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;
		ArrayList<History> histories = new ArrayList<History>();
		try {
			if (con != null) {
				stmt = con
						.prepareStatement("SELECT t.userid,u.username,u.firstname,u.lastname,"
								+ "(uf.address).addLine1 addLine1,"
								+ "(uf.address).addLine2 addLine2,"
								+ "(uf.address).addLine3 addLine3,"
								+ "(uf.address).city city,(uf.address).state state,"
								+ "(uf.address).phone phones,uf.empnumber,"
								+ "t.effectdate,t.enddate"
								+ " from "
								+ schemaName
								+ ".userterritorymaphistory as t,"
								+ schemaName
								+ ".userprofile uf,"
								+ " master.users as u"
								+ " where t.terrid = ? AND t.userid = u.id AND t.userid = uf.userid");

				stmt.setInt(1,id);
				result = stmt.executeQuery();
				while (result.next()) {
					History history = new History();
					history.userid = result.getInt(1);
					history.username = result.getString(2);
					history.firstname = result.getString(3);
					history.lastname = result.getString(4);
					history.addLine1 = result.getString(5);
					history.addLine2 = result.getString(6);
					history.addLine3 = result.getString(7);
					history.city = result.getString(8);
					history.state = result.getString(9);
					if (result.getArray(10) != null)
						history.phones = (String[]) result.getArray(10)
								.getArray();
					history.empnumber = result.getString(11);
					if(!result.getTimestamp(12).equals(result.getTimestamp(13))) {
						history.effectDate = result.getTimestamp(12);
						history.endDate = result.getTimestamp(13);
					}
					else
					{
						history.effectDate = result.getTimestamp(12);
						history.endDate = null;

					}
					history.isHistory = true;
					histories.add(history);
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
		return histories;
	}
}