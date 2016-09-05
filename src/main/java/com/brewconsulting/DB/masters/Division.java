package com.brewconsulting.DB.masters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.brewconsulting.DB.utils;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.brewconsulting.exceptions.RequiredDataMissing;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Division {

	@JsonProperty("id")
	public int id;

	@JsonProperty("name")
	public String name;

	@JsonProperty("description")
	public String description;

	@JsonProperty("createDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
	public Date createDate;

	@JsonProperty("createBy")
	public int createBy;

	@JsonProperty("updateDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
	public Date updateDate;

	@JsonProperty("updateBy")
	public int updateBy;

	// make the default constructor visible to package only.
	Division() {

	}

	public static List<Division> getAllDivisions(LoggedInUser loggedInUser) throws Exception {
		// TODO: check authorization of the user to see this data
		String schemaName = loggedInUser.schemaName;

		Connection con = DBConnectionProvider.getConn();
		ArrayList<Division> divisions = new ArrayList<Division>();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con.prepareStatement("select id, name, description, createDate, createBy, updateDate, "
						+ " updateBy from " + schemaName + ".divisions");
				result = stmt.executeQuery();
				while (result.next()) {
					Division div = new Division();
					div.id = result.getInt(1);
					div.name = result.getString(2);
					div.description = result.getString(3);
					div.createDate = result.getTimestamp(4);
					div.createBy = result.getInt(5);
					div.updateDate = result.getTime(6);
					div.updateBy = result.getInt(7);
					// System.out.println(div.createDate.);
					divisions.add(div);
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
		return divisions;
	}

	public static Division getDivisionById(int id, LoggedInUser loggedInUser) throws Exception {
		Division division = null;
		// TODO check authorization
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con.prepareStatement("select id, name, description, createDate, createBy, updateDate, "
						+ " updateBy from " + schemaName + ".divisions where id = ?");
				stmt.setInt(1, id);
				result = stmt.executeQuery();
				if (result.next()) {
					division = new Division();
					division.id = result.getInt(1);
					division.name = result.getString(2);
					division.description = result.getString(3);
					division.createDate = result.getTimestamp(4);
					division.createBy = result.getInt(5);
					division.updateDate = result.getTime(6);
					division.updateBy = result.getInt(7);
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

		return division;
	}
}
