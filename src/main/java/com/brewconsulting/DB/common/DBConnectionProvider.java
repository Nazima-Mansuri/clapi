package com.brewconsulting.DB.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/***
 * This class should be removed later and Tomcat connection pooling for Postgres should be used.
 * @author apple
 *
 */

public class DBConnectionProvider {
	public static Connection getConn() throws ClassNotFoundException, SQLException{
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://rollapg.chuxpgoly7kv.us-east-1.rds.amazonaws.com/Rolla";
		Properties props = new Properties();
		props.setProperty("user", "root");
		props.setProperty("password", "Rolla!2#4");
		conn = DriverManager.getConnection(url,props);

		return conn;
	}
}
