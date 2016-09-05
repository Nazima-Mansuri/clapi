package com.brewconsulting.DB;

import com.brewconsulting.exceptions.RequiredDataMissing;
import com.fasterxml.jackson.databind.JsonNode;

public class utils {

	public static String getSchemaName(JsonNode loggedInUser) throws RequiredDataMissing{
		if (!loggedInUser.has("schemaName"))
			throw new RequiredDataMissing("JTW token does not have schema name");
		
		if (loggedInUser.get("schemaName").asText().trim().length() < 1)
			throw new RequiredDataMissing("JWT has empty schema name");
		
		return loggedInUser.get("schemaName").asText().trim();
	}
	
}
