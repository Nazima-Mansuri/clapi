package com.brewconsulting.DB.masters;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {
	@JsonProperty("roleid")
	public int roleId;
	@JsonProperty("rolename")
	public String roleName;
	
	public Role(){
		
	}
	public Role (int id, String name){
		this.roleId = id;
		this.roleName = name;
	}
}
