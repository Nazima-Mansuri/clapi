package com.brewconsulting.DB.masters;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Territorie {
	@JsonProperty("id")
	public int id;

	@JsonProperty("name")
	public String name;
	
	@JsonProperty("address")
	public String address;
	
	@JsonProperty("contactNo")
	public String contactNo;

	@JsonProperty("parentId")
	public int parentId;

	@JsonProperty("personId")
	public int personId;

	@JsonProperty("divId")
	public int divId;
	
	// make the default constructor visible to package only.
	public Territorie() {
		
	}

	/***
	 * Method allows user to get All Details of Territorie.
	 * 
	 * @param loggedInUser
	 * @return
	 * @throws Exception
	 */
	public static List<Territorie> getAllTerritories(LoggedInUser loggedInUser)
			throws Exception {
		ArrayList<Territorie> territories = new ArrayList<Territorie>();

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
	public static Territorie getTerritorieById(int id, LoggedInUser loggedInUser)
			throws Exception {
		Territorie territorie = null;

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
		return 0;
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
		return 0;
	}

	/***
	 * Method allows user to Delete Territorie from Database.
	 * 
	 * @param loggedInUser
	 * @param id
	 * @throws Exception
	 */

	public static int deleteTerritorie(int id, LoggedInUser loggedInUser)
			throws Exception {
		return 0;
	}

}
