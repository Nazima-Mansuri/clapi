package com.brewconsulting.DB.masters;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lcom53 on 17/11/16.
 */
public class UserDetail {

    @JsonProperty("userId")
    public int userId;

    @JsonProperty("username")
    public String username;

    @JsonProperty("firstname")
    public String firstname;

    @JsonProperty("lastname")
    public String lastname;

    @JsonProperty("city")
    public String city;

    @JsonProperty("state")
    public String state;

    @JsonProperty("phones")
    public String[] phones;

    public UserDetail() {
    }

    public UserDetail(int userId, String username, String firstname, String lastname, String city, String state, String[] phones) {
        this.userId = userId;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.city = city;
        this.state = state;
        this.phones = phones;
    }
}
