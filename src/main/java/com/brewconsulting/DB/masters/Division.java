package com.brewconsulting.DB.masters;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

	public static List<Division> getAllDivisions(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		String schemaName = loggedInUser.schemaName;

		Connection con = DBConnectionProvider.getConn();
		ArrayList<Division> divisions = new ArrayList<Division>();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con
						.prepareStatement("select id, name, description, createDate, createBy, updateDate, "
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

	public static Division getDivisionById(int id, LoggedInUser loggedInUser)
			throws Exception {
		Division division = null;
		// TODO check authorization
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con
						.prepareStatement("select id, name, description, createDate, createBy, updateDate, "
								+ " updateBy from "
								+ schemaName
								+ ".divisions where id = ?");
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

	public static int addDivision(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Insert this data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result;

		try {
			con.setAutoCommit(false);

			stmt = con
					.prepareStatement(
							"INSERT INTO "
									+ schemaName
									+ ".divisions(name,description,createDate,createBy) values (?,?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, node.get("name").asText());
			stmt.setString(2, node.get("description").asText());
			stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
			stmt.setInt(4, loggedInUser.id);
			result = stmt.executeUpdate();

			if (result == 0)
				throw new SQLException("Add Division Failed.");

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			int divisionId;
			if (generatedKeys.next())
				divisionId = generatedKeys.getInt(1);
			else
				throw new SQLException("No ID obtained");

			con.commit();
			return divisionId;

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

	public static int updateDivision(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Update this data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result;

		try {
			// It checks if connection is not null then perform update operation.
			if (con != null) { 
				stmt = con
						.prepareStatement("UPDATE "
								+ schemaName
								+ ".divisions SET name = ?,description = ?,updateDate = ?,"
								+ "updateBy = ? WHERE id = ?");
				stmt.setString(1, node.get("name").asText());
				stmt.setString(2, node.get("description").asText());
				stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
				stmt.setInt(4, loggedInUser.id);
				stmt.setInt(5, node.get("divisionId").asInt());

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
	}

	public static void deleteDivision(int id , LoggedInUser loggedInUser) throws Exception {
		// TODO: check authorization of the user to Delete this data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result;

		try {
			// If connection is not null then perform delete operation.
			if (con != null) {
				stmt = con
						.prepareStatement("DELETE FROM "+ schemaName + ".divisions WHERE id = ?");

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
	}
}
