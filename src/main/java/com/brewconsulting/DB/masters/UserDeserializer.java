package com.brewconsulting.DB.masters;

import java.io.IOException;
import java.util.Iterator;

import com.brewconsulting.DB.masters.User.Irole;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.node.IntNode;

public class UserDeserializer extends StdDeserializer<UserProfile> {

	protected UserDeserializer(Class<?> vc) {
		super(vc);		
	}
	public UserDeserializer(){
		this(null);
	}

	@Override
	public UserProfile deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		int id = (Integer)((IntNode) node.get("id")).numberValue();
				
		UserProfile user = new UserProfile(id);
		user.clientId = (Integer)((IntNode) node.get("clientId")).numberValue();
		user.username = node.get("clientId").asText();
		user.schemaName = node.get("schemaName").asText();
		user.firstName = node.get("firstName").asText();
		user.lastName = node.get("lastName").asText();
		JsonNode rolesNode = node.get("roles");
		Iterator<JsonNode> it = rolesNode.elements();
		ObjectMapper mapper = new ObjectMapper();
		
		while(it.hasNext()){
			JsonNode n = it.next();
			System.out.println(n.get("name"));
			
		}
//		
//		while (it.hasNext()) {
//			final JsonNode roleNode = it.next();
//			System.out.println(roleNode.get("rolename").asText());
//			user.roles.add(new Irole() {
//				public int id = roleNode.get("roleid").asInt();
//				public String name = roleNode.get("rolename").asText();
//			});
//		}
		
		return user;
	}

}
