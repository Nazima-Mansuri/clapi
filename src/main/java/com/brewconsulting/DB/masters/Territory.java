package com.brewconsulting.DB.masters;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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

	@JsonProperty("effectDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
	public Date effectDate;

	@JsonProperty("createDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
	public Date createDate;

	@JsonProperty("createBy")
	public int createBy;

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
				stmt = con
						.prepareStatement("select id, name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
								+ "(address).addLine3 addLine3,(address).city city,(address).state state,"
								+ "(address).phone phones,parentId,divId from "
								+ schemaName + ".territories");
				result = stmt.executeQuery();
				while (result.next()) {
					Territory terr = new Territory();
					terr.id = result.getInt(1);
					terr.name = result.getString(2);
					terr.addLine1 = result.getString(3);
					terr.addLine2 = result.getString(4);
					terr.addLine3 = result.getString(5);
					terr.city = result.getString(6);
					terr.state = result.getString(7);
					// If phone number is null then it gives null pointer
					// exception here.
					// So it check that the phone number is null or not
					if (result.getArray(8) != null)
						terr.phones = (String[]) result.getArray(8).getArray();
					terr.parentId = result.getInt(9);
					terr.divId = result.getInt(10);

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

		Territory territory = null;
		// TODO check authorization
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con
						.prepareStatement("select id, name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
								+ "(address).addLine3 addLine3,(address).city city,(address).state state,(address).phone phones,"
								+ "parentId,divId from "
								+ schemaName
								+ ".territories where id = ?");
				stmt.setInt(1, id);
				result = stmt.executeQuery();
				if (result.next()) {
					territory = new Territory();
					territory.id = result.getInt(1);
					territory.name = result.getString(2);
					territory.addLine1 = result.getString(3);
					territory.addLine2 = result.getString(4);
					territory.addLine3 = result.getString(5);
					territory.city = result.getString(6);
					territory.state = result.getString(7);
					// If phone number is null then it gives null pointer
					// exception here.
					// So it check that the phone number is null or not
					if (result.getArray(8) != null)
						territory.phones = (String[]) result.getArray(8)
								.getArray();
					territory.parentId = result.getInt(9);
					territory.divId = result.getInt(10);
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

		return territory;
	}

	/***
	 * Method allows user to insert Territorie in Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static int addTerritory(JsonNode node, LoggedInUser loggedInUser)
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
			int territoryId;
			if (generatedKeys.next())
				// It gives last inserted Id from territory
				territoryId = generatedKeys.getInt(1);
			else
				throw new SQLException("No ID obtained");
			
			if (node.get("personId") != null) {
				System.out.println("Schema Name : " + schemaName);
				stmt = con
						.prepareStatement("INSERT INTO "
								+ schemaName
								+ ".userterritorymap"
								+ "(userId,terrId,effectDate,createBy,createDate) values (?,?,?,?,?)");
				stmt.setInt(1, node.get("personId").asInt());
				stmt.setInt(2, territoryId);
				stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
				stmt.setInt(4, loggedInUser.id);
				stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));

				stmt.executeUpdate();

				stmt = con
						.prepareStatement("INSERT INTO "
								+ schemaName
								+ ".userterritorymaphistory"
								+ "(userId,terrId,effectDate,endDate,createBy,createDate) values (?,?,?,?,?,?)");

				stmt.setInt(1, node.get("personId").asInt());
				stmt.setInt(2, territoryId);
				stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
				stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
				stmt.setInt(5, loggedInUser.id);
				stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

				stmt.executeUpdate();
			}

			con.commit();
			return territoryId;

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
	 * Then insert data in userTerritoryMap and userTerritoryMapHistory
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static int updateTerritory(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Update data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int affectedRow;

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

				affectedRow = stmt.executeUpdate();
	
				// It Insert data in userTerritoryMap with new userId
					stmt = con
							.prepareStatement("INSERT INTO "
									+ schemaName
									+ ".userterritorymap"
									+ "(userId,terrId,effectDate,createBy,createDate) values (?,?,?,?,?)");
					
					stmt.setInt(1, node.get("personId").asInt());
					stmt.setInt(2, node.get("id").asInt());
					stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
					stmt.setInt(4, loggedInUser.id);
					stmt.setTimestamp(5, new Timestamp((new Date()).getTime()));
					
					stmt.executeUpdate();
						
					// It Insert data in userTerritoryMapHistory with new userId
					stmt = con
							.prepareStatement("INSERT INTO "
									+ schemaName
									+ ".userterritorymaphistory"
									+ "(userId,terrId,effectDate,endDate,createBy,createDate) values (?,?,?,?,?,?)");

					stmt.setInt(1, node.get("personId").asInt());
					stmt.setInt(2, node.get("id").asInt());
					stmt.setTimestamp(3, new Timestamp((new Date()).getTime()));
					stmt.setTimestamp(4, new Timestamp((new Date()).getTime()));
					stmt.setInt(5, loggedInUser.id);
					stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

					stmt.executeUpdate();

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
		return affectedRow;
	}

	/***
	 * Method allows user to Delete Territorie from Database.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @throws Exception
	 * @Return
	 */

	public static int deleteTerritory(int id, LoggedInUser loggedInUser)
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

	/***
	 * Method allows user to Delete person from userMapTerritorie and 
	 * update endDate in userTerritoryMapHistory Tables from Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @throws Exception
	 * @Return
	 */
	@SuppressWarnings("resource")
	public static int deassociateUser(JsonNode node, LoggedInUser loggedInUser) throws Exception {
		// TODO: check authorization 
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result;
		int userId = 0;
		int affectedRow;
		
		try
		{
			if(con != null)
			{
				//It gets userId from userterritorymap table 
				stmt = con.prepareStatement("SELECT userId from " + schemaName
						+ ".userterritorymap " + "where terrId = ?");
				stmt.setInt(1, node.get("id").asInt());
				result = stmt.executeQuery();
				if (result.next())
					userId = result.getInt(1);
				System.out.println("UserId : " + userId);
				
				// It delete entry of deassociate person from userTerritoryMap table .
				stmt = con.prepareStatement("DELETE from "+ schemaName +".userterritorymap "
						+ "where userId = ? AND terrId = ?");
				stmt.setInt(1, userId);
				stmt.setInt(2,node.get("id").asInt());
				affectedRow = stmt.executeUpdate();
				System.out.println("affectedRow : " + affectedRow);
				
				// It update endDate of deassociate person in userTerritoryMapHistory table  .
				stmt = con
						.prepareStatement("UPDATE "
								+ schemaName
								+ ".userterritorymaphistory SET endDate = ? WHERE userId = ? AND terrId = ?");
			    stmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
			    stmt.setInt(2, userId);
			    stmt.setInt(3, node.get("id").asInt());
			    stmt.executeUpdate();
				
			}
			else
				throw new Exception("DB connection is null");
		}
		finally {

			if (stmt != null)
				if (!stmt.isClosed())
					stmt.close();
			if (con != null)
				if (!con.isClosed())
					con.close();
		}
		return affectedRow;
	}

}
