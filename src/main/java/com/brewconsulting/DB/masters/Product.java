package com.brewconsulting.DB.masters;

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

public class Product {

	@JsonProperty("id")
	public int id;

	@JsonProperty("name")
	public String name;

	@JsonProperty("description")
	public String description;

	@JsonProperty("image")
	public String image;

	@JsonProperty("isActive")
	public Boolean isActive;

	@JsonProperty("division")
	public int division;

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

	public Product() {

	}

	/***
	 * Method allows user to get All Details of Products.
	 * 
	 * @param loggedInUser
	 * @return
	 * @throws Exception
	 */
	public static List<Product> getAllProducts(LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to see this data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		ArrayList<Product> products = new ArrayList<Product>();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con
						.prepareStatement("select id, name,image, description,division,isActive, createDate,"
								+ "createBy, updateDate,updateBy from "
								+ schemaName + ".products");
				result = stmt.executeQuery();
				while (result.next()) {
					Product product = new Product();
					product.id = result.getInt(1);
					product.name = result.getString(2);
					product.image = result.getString(3);
					product.description = result.getString(4);
					product.division = result.getInt(5);
					product.isActive = result.getBoolean(6);
					product.createDate = result.getTimestamp(7);
					product.createBy = result.getInt(8);
					product.updateDate = result.getTime(9);
					product.updateBy = result.getInt(10);
					products.add(product);
				}
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

		return products;
	}

	/***
	 * Method allows user to get Details of Particular Product.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static Product getProductById(int id, LoggedInUser loggedInUser)
			throws Exception {
		Product product = null;
		// TODO check authorization
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			if (con != null) {
				stmt = con
						.prepareStatement("select id, name,image,description,division,isActive, createDate,"
								+ "createBy, updateDate,updateBy from "
								+ schemaName
								+ ".products where id = ?");
				stmt.setInt(1, id);
				result = stmt.executeQuery();
				if (result.next()) {
					product = new Product();
					product.id = result.getInt(1);
					product.name = result.getString(2);
					product.image = result.getString(3);
					product.description = result.getString(4);
					product.division = result.getInt(5);
					product.isActive = result.getBoolean(6);
					product.createDate = result.getTimestamp(7);
					product.createBy = result.getInt(8);
					product.updateDate = result.getTime(9);
					product.updateBy = result.getInt(10);
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
		return product;

	}

	/***
	 * Method allows user to insert Product in Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static int addProduct(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Insert data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result = 0;

		try {
			con.setAutoCommit(false);

			stmt = con
					.prepareStatement(
							"INSERT INTO "
									+ schemaName
									+ ".products(name,image,description,division,isActive,createDate,"
									+ "createBy,updateDate,updateBy) values (?,?,?,?,?,?,?,?,?)",
							Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1, node.get("name").asText());
			
			if(node.has("image"))
				stmt.setString(2, node.get("image").asText());
			else
				stmt.setString(2, null);
			
			// It checks that description is empty or not
			if (node.has("description"))
				stmt.setString(3, node.get("description").asText());
			else
				stmt.setString(3, null);

			stmt.setInt(4, node.get("division").asInt());

			// Checks isActive empty or not
			if (node.has("isActive"))
				stmt.setBoolean(5, node.get("isActive").asBoolean());
			else
				// If isActive empty it set default TRUE
				stmt.setBoolean(5, true);
			
			stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
			stmt.setInt(7,loggedInUser.id);
			stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
			stmt.setInt(9,loggedInUser.id);
			result = stmt.executeUpdate();
			
			if (result == 0)
				throw new SQLException("Add Product Failed.");

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			int productId;
			if (generatedKeys.next())
				// It gives last inserted Id in divisionId
				productId = generatedKeys.getInt(1);
			else
				throw new SQLException("No ID obtained");

			con.commit();
			return productId;
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
	 * Method allows user to Update Product in Database.
	 * 
	 * @param loggedInUser
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static int updateProduct(JsonNode node, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Update data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result = 0;
		try {
			if (con != null) {
				stmt = con
						.prepareStatement("UPDATE "
								+ schemaName
								+ ".products SET name = ?,image = ?,description = ?,division = ?,isActive = ?"
								+ ",updateDate = ?, updateBy = ? WHERE id = ?");
				stmt.setString(1, node.get("name").asText());
				if(node.has("image"))
					stmt.setString(2, node.get("image").asText());
				else
					stmt.setString(2, null);
				stmt.setString(3, node.get("description").asText());
				stmt.setInt(4, node.get("division").asInt());
				stmt.setBoolean(5, node.get("isActive").asBoolean());
				stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
				stmt.setInt(7,loggedInUser.id);
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
	 * Method allows user to Delete Product from Database.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @throws Exception
	 * @Return
	 */

	public static int deleteProduct(int id, LoggedInUser loggedInUser)
			throws Exception {
		// TODO: check authorization of the user to Delete data
		String schemaName = loggedInUser.schemaName;
		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		int result = 0;

		try {
			if (con != null) {
				stmt = con.prepareStatement("DELETE FROM " + schemaName
						+ ".products WHERE id = ?");

				stmt.setInt(1, id);
				result = stmt.executeUpdate();
			} else
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
		return result;
	}

}
