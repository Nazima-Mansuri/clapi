package com.brewconsulting.DB;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.brewconsulting.exceptions.RequiredDataMissing;
import com.fasterxml.jackson.databind.JsonNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class utils {

	public static String getSchemaName(JsonNode loggedInUser) throws RequiredDataMissing{
		if (!loggedInUser.has("schemaName"))
			throw new RequiredDataMissing("JTW token does not have schema name");
		
		if (loggedInUser.get("schemaName").asText().trim().length() < 1)
			throw new RequiredDataMissing("JWT has empty schema name");
		
		return loggedInUser.get("schemaName").asText().trim();
	}
	
	public static Response getErrorResponse(Exception e){
		return Response.serverError().header("content-type", MediaType.TEXT_PLAIN).entity(e.getMessage()).build();
	}

	/**
	 * This Method is used to convert String date format to Date format
	 *
	 * @param strDate
	 * @return
	 * @throws ParseException
	 */
	public static Date stringToDate(String strDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		java.util.Date date = sdf.parse(strDate);

		return date;
	}
}
