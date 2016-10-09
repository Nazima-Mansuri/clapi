package com.brewconsulting.DB.masters;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoggedInUser {
	@JsonProperty("clientId")
	public int clientId;
	@JsonProperty("id")
	public int id;
	@JsonProperty("username")
	public String username;
	@JsonProperty("schemaName")
	public String schemaName;
	@JsonProperty("firstName")
	public String firstName;
	@JsonProperty("lastName")
	public String lastName;
	@JsonProperty("roles")
	public ArrayList<Role> roles;
	@JsonProperty("designation")
	public String designation;
}
