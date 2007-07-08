package org.deltava.dao;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

import junit.framework.TestCase;

public abstract class AbstractDAOTestCase extends TestCase {
    
    protected Connection _con;

    protected void setUp() throws Exception {
        super.setUp();
        
        Properties props = new Properties();
        props.load(new FileInputStream("data/jdbc.properties"));

        Class.forName(props.getProperty("driver"));
        DriverManager.setLoginTimeout(3);
        _con = DriverManager.getConnection(props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
    }
    
    protected void tearDown() throws Exception {
        _con.close();
        super.tearDown();
    }
}