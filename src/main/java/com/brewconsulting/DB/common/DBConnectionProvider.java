package com.brewconsulting.DB.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.tomcat.jdbc.pool.*;


/***
 * Tomcat based connection pooling done on 10Sep 2016. Later get the values from
 * properties file.
 *
 * @author apple
 */

public class DBConnectionProvider {
    public static Connection getConn() {
        // Connection conn = null;
        // Class.forName("org.postgresql.Driver");
        // String url =
        // "jdbc:postgresql://rollapg.chuxpgoly7kv.us-east-1.rds.amazonaws.com/Rolla";
        // Properties props = new Properties();
        // props.setProperty("user", "root");
        // props.setProperty("password", "Rolla!2#4");
        // conn = DriverManager.getConnection(url,props);
        /*javax.naming.Context env = null;
        env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");

		PoolProperties p = new PoolProperties();
		// p.setUrl("jdbc:postgresql://rollapg.chuxpgoly7kv.us-east-1.rds.amazonaws.com/Rolla");
		p.setUrl((String) env.lookup("DB_URL"));
		p.setDriverClassName((String) env.lookup("DB_DRIVER"));

		p.setUsername((String) env.lookup("DB_USERNAME"));
		p.setPassword((String) env.lookup("DB_PASSWORD"));
		p.setJmxEnabled((Boolean) env.lookup("DB_JMX"));
		p.setTestWhileIdle((Boolean) env.lookup("DB_TESTIDLE"));
		p.setTestOnBorrow((Boolean) env.lookup("DB_TESTBORROW"));
		p.setValidationQuery((String) env.lookup("DB_VALQUERY"));
		p.setTestOnReturn((Boolean) env.lookup("DB_TESTRETURN"));
		p.setValidationInterval((Long) env.lookup("DB_VALINTERVAL"));
		p.setTimeBetweenEvictionRunsMillis((Integer) env.lookup("DB_EVRUNINTERVAL"));
		p.setMaxActive((Integer) env.lookup("DB_MAXACTIVE"));
		p.setInitialSize((Integer) env.lookup("DB_INITIALSIZE"));
		p.setMaxWait((Integer) env.lookup("DB_MAXWAIT"));
		p.setRemoveAbandonedTimeout((Integer) env.lookup("DB_REMOVEABANTIMEOUT"));
		p.setMinEvictableIdleTimeMillis((Integer) env.lookup("DB_MINEVIDLETIME"));
		p.setMinIdle((Integer) env.lookup("DB_MINIDLE"));
		p.setLogAbandoned((Boolean)env.lookup("DB_SETLOGABAN"));
		p.setRemoveAbandoned((Boolean)env.lookup("DB_SETREMABAN"));
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");



		InitialContext ic = new InitialContext();
		DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/postgres");
		return ds.getConnection();*/

		/*DataSource datasource = new DataSource("jdbc/postgres");
		datasource.setPoolProperties(p);

		return datasource.getConnection();
*/
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//		String url = "jdbc:postgresql://rollapg.chuxpgoly7kv.us-east-1.rds.amazonaws.com/Rolla";
//		Properties props = new Properties();
//		props.setProperty("user", "root");
//		props.setProperty("password", "Rolla!2#4");

//		String url = "jdbc:postgresql://localhost/rolla";
//		Properties props = new Properties();
//		props.setProperty("user", "postgres");
//		props.setProperty("password", "root");

        String url = "jdbc:postgresql://34.231.6.39/rolla";
        Properties props = new Properties();
        props.setProperty("user", "rolladb");
        props.setProperty("password", "RoLL@1@#$");
        try {
            conn = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
        // return conn;
    }
}
