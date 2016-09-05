package com.brewconsulting.exceptions;

public class RequiredDataMissing extends Exception{
	public static final int ErrorCode = 9000;  
	public RequiredDataMissing(String s){
		super(s);
	}
	public String getJsonString(){
		return "{\"errorCode\":"+ErrorCode+",\"errorText\":"+this.getMessage()+"}";
	}

}
