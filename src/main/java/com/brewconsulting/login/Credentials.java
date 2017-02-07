package com.brewconsulting.login;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Credentials implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1819777009078056892L;
	private String username;
	private String password;
	private boolean isPublic = true;
	private String accessToken;
	private String refreshToken;
	private String deviceToken;
	private String deviceOS;

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getDeviceOS() {
		return deviceOS;
	}

	public void setDeviceOS(String deviceOS) {
		this.deviceOS = deviceOS;
	}

	public Credentials(){
		
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean getIsPublic(){
		return isPublic;
	}
	public void setIsPublic(boolean isPublic){
		this.isPublic = isPublic;
	}
	public String getAccessToken(){
		return accessToken;
	}
	public void setAccessToken(String accessToken){
		this.accessToken = accessToken;
	}
	public String getRefreshToken(){
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken){
		this.refreshToken = refreshToken;
	}
	
}
