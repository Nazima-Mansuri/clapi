package com.brewconsulting.DB;

import java.util.Date;
import java.util.List;

public class UserProfile extends User {
	
	public String addLine1;
	public String addLine2;
	public String addLine3;
	public String city;
	public String state;
	public List<String> contactNumbers;
	public String designation;
	public int divId;
	public String divname;
	public String empNum;
	public Date createDate;
	public int createBy;
	public Date modifiedDate;
	public int modifiedBy;
	
	UserProfile(){
		
	}
	UserProfile(User u){
		super.id = u.id;
		super.clientId = u.clientId;
		super.roles = u.roles;
		super.schemaName = u.schemaName;
		super.firstName = u.firstName;
		super.lastName = u.lastName;
		super.username = u.username;
	}
}
