package com.brewconsulting.exceptions;

public class NoDataFound extends Exception {
	public static final int ErrorCode = 9001;  
	public NoDataFound(String s){
		super(s);
	}
	public String getJsonString(){
		return "{\"errorCode\":"+ErrorCode+",\"errorText\":"+this.getMessage()+"}";
	}

}
