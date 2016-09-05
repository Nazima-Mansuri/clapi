package com.brewconsulting.DB.masters;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {
	@JsonProperty("roleid")
	public int roleId;
	@JsonProperty("rolename")
	public String roleName;
}
