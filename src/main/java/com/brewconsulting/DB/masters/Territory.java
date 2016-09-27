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

import javax.jws.soap.SOAPBinding.Use;

import com.brewconsulting.DB.User;
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

				// In this query e1 type field gives Parent field data and 
				//e2 type field gives Child field data. 
/*				stmt = con.prepareStatement("select "
						+ "e1.id,e1.name,(e1.address).addLine1 addLine1,"
						+ "(e1.address).addLine2 addLine2,"
						+ "(e1.address).addLine3 addLine3,(e1.address).city city,(e1.address).state state,"
						+ "(e1.address).phone phones,"
						+ "e1.parentid,e1.divid, e2.id,e2.name"
						+ " from client1.territories e1"
						+ " inner join client1.territories e2 on e2.parentid = e1.id");*/
			
				
				// This query return all data of table
				stmt = con
						.prepareStatement("select id,name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
								+ "(address).addLine3 addLine3,(address).city city,(address).state state,"
								+ "(address).phone phones,parentId,divId from "
								+ schemaName + ".territories ORDER BY id DESC");
				
				// This query return data in json format in one row
				/*stmt = con.prepareStatement("select row_to_json(row)"
						+ "from (select u.id,u.name,(u.address).addLine1 addLine1,"
						+ "urd AS children from client1.territories u "
						+ " inner join client1.territories urd (id,name)"
						+ " on urd.parentid = u.id) row");*/
				
				// We Need Data in This Type of Format When parent has child
				/** {
				 *  	{ text:'Root',
                                TID:1,
                                data:{ TID:1,
                                          personId:1,
                                          personName:'Test1',
                                          ParentID:0,
                                          name:'Root',
                                          addLine1 :'address1 of Root',
                                          addLine2 :'address2 of Root',
                                          addLine3 :'address3 of Root',
                                          city        :'Surat',
                                          state    :'Gujarat',
                                          contactnumber : ['8878785875','8787874875','7777777777','5748789887','8787878589'] ,                                      },
                                   children:[{ 
                                               text:'Root1_Parent1',
                                               data:{
                                                     TID:4,
                                                     apersonID:2,
                                                     personName:'Associate1',
                                                     ParentID:0,
                                                     name:'Root1_Parent1',
                                                     addLine1 :'address1 of Root',
                                                     addLine2 :'address2 of Root',
                                                     addLine3 :'address3 of Root',
                                                     city        :'Surat',
                                                     state    :'Gujarat',
                                                     contactnumber : ['8878785875','8787874875','7777777777','5748789887','8787878589'],
                                                    },
                                                    
                                                    N number of child possible here.
                                            }]
                             }
                         }
				 */
				
				// If Parent has No child Then data 
			/**
			 * {
				   	{ text:'Root',
                                TID:1,
                                data:{ TID:1,
                                          personId:1,
                                          personName:'Test1',
                                          ParentID:0,
                                          name:'Root',
                                          addLine1 :'address1 of Root',
                                          addLine2 :'address2 of Root',
                                          addLine3 :'address3 of Root',
                                          city        :'Surat',
                                          state    :'Gujarat',
                                          contactnumber : ['8878785875','8787874875','7777777777','5748789887','8787878589'] ,                                      },
                     }
                 }                 
			 */
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
					// If phone number is null then it gives null pointer exception here.
					// So it check that the phone number is null or not
					if (result.getArray(8) != null)
					terr.phones = (String[]) result.getArray(8).getArray();
					
					terr.parentId = result.getInt(9);
//					terr.addLine1 = result.getString(4);
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
								+ "(address).addLine3 addLine3,(address).city city,(address).state state,"
								+ "(address).phone phones,"
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
					// If phone number is null then it gives null pointer exception here.
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
	
	public static List<Territory> getTerritorieByDivisionId(int id, LoggedInUser loggedInUser)
			throws Exception {

		Territory territory = null;
		// TODO check authorization
		String schemaName = loggedInUser.schemaName;
		ArrayList<Territory> territories = new ArrayList<Territory>();
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {

				stmt = con
						.prepareStatement("select id,name,(address).addLine1 addLine1, (address).addLine2 addLine2,"
								+ "(address).addLine3 addLine3,(address).city city,(address).state state,"
								+ "(address).phone phones,parentId,divId from "
								+ schemaName + ".territories WHERE divid = ?");
				stmt.setInt(1,id);

				result = stmt.executeQuery();
				while (result.next()) {
					territory = new Territory();
					territory.id = result.getInt(1);
					territory.name = result.getString(2);
					territory.addLine1 = result.getString(3);
					territory.addLine2 = result.getString(4);
					territory.addLine3 = result.getString(5);
					territory.city = result.getString(6);
					territory.state = result.getString(7);
					// If phone number is null then it gives null pointer exception here.
					// So it check that the phone number is null or not
					if (result.getArray(8) != null)
						territory.phones = (String[]) result.getArray(8)
								.getArray();
					territory.parentId = result.getInt(9);
					territory.divId = result.getInt(10);
					
					territories.add(territory);
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
		int affectedRow;
		ResultSet result = null;
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
			if(node.has("parentId"))
				stmt.setInt(2, node.get("parentId").asInt());
			else
				stmt.setInt(2, 0);

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

			affectedRow = stmt.executeUpdate();

			if (affectedRow == 0)
				throw new SQLException("Add Territorie Failed.");

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			int territoryId;
			if (generatedKeys.next())
				// It gives last inserted Id from territory
				territoryId = generatedKeys.getInt(1);
			else
				throw new SQLException("No ID obtained");

			if (node.get("personId") != null) {
				stmt = con.prepareStatement("SELECT userId from " + schemaName
						+ ".userterritorymap WHERE userId = ?");

				stmt.setInt(1, node.get("personId").asInt());
				result = stmt.executeQuery();

				if (!result.next()) {
					System.out.println("In If Method..");
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
				} else {
					System.out.println("In Else Method..");
				}
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
	 * Method allows user to Update Territorie in Database. Then insert data in
	 * userTerritoryMap and userTerritoryMapHistory
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
		ResultSet result;

		try {
			con.setAutoCommit(false);
			// It checks if connection is not null then perform update
			// operation.
			String[] phoneArr = new String[node.withArray("phones").size()];

			// Convert JsonArray into String Array
			for (int i = 0; i < node.withArray("phones").size(); i++) {
				phoneArr[i] = node.withArray("phones").get(i).asText();
			}

			Array pharr = con.createArrayOf("text", phoneArr);

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

			stmt = con.prepareStatement("SELECT count(*) as Resultcount from "
					+ schemaName + ".userterritorymap WHERE userId = ?");
			stmt.setInt(1, node.get("personId").asInt());
			result = stmt.executeQuery();

			if (!result.next()) {
				System.out.println("If Method ");
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
			}

			con.commit();
			return affectedRow;
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
	 * Method allows user to Delete person from userMapTerritorie and update
	 * endDate in userTerritoryMapHistory Tables from Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @throws Exception
	 * @Return
	 */
	@SuppressWarnings("resource")
	public static int deassociateUser(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result;
		int userId = 0;
		int affectedRow;

		try {
			if (con != null) {
				// It gets userId from userterritorymap table
				stmt = con.prepareStatement("SELECT userId from " + schemaName
						+ ".userterritorymap " + "where terrId = ?");
				stmt.setInt(1, node.get("id").asInt());
				result = stmt.executeQuery();
				if (result.next())
					userId = result.getInt(1);
				System.out.println("UserId : " + userId);

				// It delete entry of deassociate person from userTerritoryMap
				// table .
				stmt = con.prepareStatement("DELETE from " + schemaName
						+ ".userterritorymap "
						+ "where userId = ? AND terrId = ?");
				stmt.setInt(1, userId);
				stmt.setInt(2, node.get("id").asInt());
				affectedRow = stmt.executeUpdate();
				System.out.println("affectedRow : " + affectedRow);

				// It update endDate of deassociate person in
				// userTerritoryMapHistory table .
				stmt = con
						.prepareStatement("UPDATE "
								+ schemaName
								+ ".userterritorymaphistory SET endDate = ? WHERE userId = ? AND terrId = ?");
				stmt.setTimestamp(1, new Timestamp((new Date()).getTime()));
				stmt.setInt(2, userId);
				stmt.setInt(3, node.get("id").asInt());
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


}
