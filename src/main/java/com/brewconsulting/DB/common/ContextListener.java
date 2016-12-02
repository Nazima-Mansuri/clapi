package com.brewconsulting.DB.common;

import com.brewconsulting.masters.Divisions;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by lcom53 on 15/11/16.
 */
public class ContextListener implements ServletContextListener {
    static final Logger logger = Logger.getLogger(ContextListener.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        DataSource source = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            javax.naming.Context env = null;
            env = (javax.naming.Context) new InitialContext().lookup("java:comp/env");
            String databasename = (String) env.lookup("DB_URL");
            source = (DataSource) (new InitialContext()).lookup(databasename);
            Connection connection = source.getConnection();
            if(connection != null) {
                if (!connection.isClosed())
                     connection.close();
            }
            // Get the webapp's ClassLoader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            // Loop through all drivers
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getClassLoader() == cl) {
                    // This driver was registered by the webapp's ClassLoader, so deregister it:
                    try {
                        System.out.println("Deregistering JDBC driver {}" + driver);
//                    log.info("Deregistering JDBC driver {}", driver);
                        DriverManager.deregisterDriver(driver);
                    } catch (SQLException ex) {
                        System.out.println("Error deregistering JDBC driver {}" + driver + ex);
//                    log.error("Error deregistering JDBC driver {}", driver, ex);
                    }
                } else {
                    // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                    System.out.println("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader" + driver);
//                log.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
                }
            }

        } catch (NamingException e) {
            logger.error("NamingException ",e);
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error("SQLException ",e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("IOException ",e);
            e.printStackTrace();
        }
    }
}
