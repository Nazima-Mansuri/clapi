package com.brewconsulting.DB.masters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class Territory {
	@JsonProperty("id")
	public int id;

	@JsonProperty("name")
	public String name;

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

	@JsonProperty("parentId")
	public int parentId;

	@JsonProperty("personId")
	public int personId;

	@JsonProperty("divId")
	public int divId;

	// make the default constructor visible to package only.
	public Territory() {

	}

	/***
	 * Method allows user to get All Details of Territorie.
	 * 
	 * @param loggedInUser
	 * @return
	 * @throws Exception
	 */
	public static List<Territory> getAllTerritories(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		String schemaName = loggedInUser.schemaName;

		Connection con = DBConnectionProvider.getConn();
		ArrayList<Territory> territories = new ArrayList<Territory>();
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			if (con != null) {
				stmt = con.prepareStatement("");
				result = stmt.executeQuery();
				while (result.next()) {
					Territory terr = new Territory();
					terr.id = result.getInt(1);
					terr.name = result.getString(2);
					/*
					 * terr.description = result.getString(3); terr.createDate =
					 * result.getTimestamp(4); terr.createBy = result.getInt(5);
					 * terr.updateDate = result.getTime(6); terr.updateBy =
					 * result.getInt(7);
					 */
					// System.out.println(div.createDate.);
					territories.add(terr);
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
		return territories;
	}

	/***
	 * Method allows user to get Details of Particular Territorie.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static Territory getTerritorieById(int id, LoggedInUser loggedInUser)
			throws Exception {

		Territory territorie = null;
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
					territorie = new Territory();
					territorie.id = result.getInt(1);
					territorie.name = result.getString(2);
					/*
					 * territorie.description = result.getString(3);
					 * territorie.createDate = result.getTimestamp(4);
					 * territorie.createBy = result.getInt(5);
					 * territorie.updateDate = result.getTime(6);
					 * territorie.updateBy = result.getInt(7);
					 */
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

		return territorie;
	}

	/***
	 * Method allows user to insert Territorie in Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static int addTerritorie(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Insert data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result;
		try {
			con.setAutoCommit(false);

			// TODO: set up the phones string array
			String[] phoneArr = new String[node.withArray("phones").size()];

			// Convert JsonArray into String Array
			for (int i = 0; i < node.withArray("phones").size(); i++) {
				phoneArr[i] = node.withArray("phones").get(i).asText();
			}

			Array pharr = con.createArrayOf("text", phoneArr);

			stmt = con
					.prepareStatement(
							"INSERT INTO "
									+ schemaName
									+ ".territories(name,parentid,address,divid) values (?,?,ROW(?,?,?,?,?,?),?)",
							Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, node.get("name").asText());
			stmt.setInt(2, node.get("parentId").asInt());

			// It checks that the address1 has value or not
			if (node.has("addLine1"))
				stmt.setString(3, node.get("addLine1").asText());
			else
				stmt.setString(3, null);
			// It checks that the address2 has value or not
			if (node.has("addLine2"))
				stmt.setString(4, node.get("addLine2").asText());
			else
				stmt.setString(4, null);
			// It checks that the address3 has value or not
			if (node.has("addLine3"))
				stmt.setString(5, node.get("addLine3").asText());
			else
				stmt.setString(5, null);

			// It checks that the city has value or not
			if (node.has("city"))
				stmt.setString(6, node.get("city").asText());
			else
				stmt.setString(6, null);

			// It checks that the state has value or not
			if (node.has("state"))
				stmt.setString(7, node.get("state").asText());
			else
				stmt.setString(7, null);

			stmt.setArray(8, pharr);

			stmt.setInt(9, node.get("divId").asInt());

			result = stmt.executeUpdate();

			if (result == 0)
				throw new SQLException("Add Territorie Failed.");

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			int territorieId;
			if (generatedKeys.next())
				// It gives last inserted Id from territory
				territorieId = generatedKeys.getInt(1);
			else
				throw new SQLException("No ID obtained");

			con.commit();
			return territorieId;

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

	/***
	 * Method allows user to Update Territorie in Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static int updateTerritorie(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Update data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result;

		try {
			// It checks if connection is not null then perform update
			// operation.
			String[] phoneArr = new String[node.withArray("phones").size()];

			// Convert JsonArray into String Array
			for (int i = 0; i < node.withArray("phones").size(); i++) {
				phoneArr[i] = node.withArray("phones").get(i).asText();
			}

			Array pharr = con.createArrayOf("text", phoneArr);

			if (con != null) {
				stmt = con
						.prepareStatement("UPDATE "
								+ schemaName
								+ ".territories SET name = ?,address =ROW(?,?,?,?,?,?) WHERE id = ?");
				stmt.setString(1, node.get("name").asText());
				stmt.setString(2, node.get("addLine1").asText());
				stmt.setString(3, node.get("addLine2").asText());
				stmt.setString(4, node.get("addLine3").asText());
				stmt.setString(5, node.get("city").asText());
				stmt.setString(6, node.get("state").asText());
				stmt.setArray(7, pharr);
				stmt.setInt(8, node.get("id").asInt());

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

	/***
	 * Method allows user to Delete Territorie from Database.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @throws Exception
	 * @Return
	 */

	public static int deleteTerritorie(int id, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Delete data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result = 0;

		try {
			// If connection is not null then perform delete operation.
			if (con != null) {
				stmt = con.prepareStatement("DELETE FROM " + schemaName
						+ ".territories WHERE id = ?");

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
	}

}
