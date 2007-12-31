package org.deltava.dao;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

import org.deltava.beans.AbstractBeanTestCase;

public class TestDAO extends AbstractBeanTestCase {

    private MockDAO _dao;
    private Connection _con;
    
    private class MockDAO extends DAO {
        
        public MockDAO(Connection c) {
            super(c);
        }
        
        public int getQueryTimeout() {
            return _queryTimeout;
        }
        
        public void initStatement() throws SQLException {
            prepareStatement("SELECT COUNT(*) FROM STAFF");
        }
        
        public void initStatement(String sql) throws SQLException {
        	prepareStatement(sql);
        }
        
        public int getQueryStart() {
            return _queryStart;
        }
        
        public int getQueryMax() {
            return _queryMax;
        }
        
        public void execute() throws SQLException {
        	_ps.execute();
        	_ps.close();
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Properties props = new Properties();
        props.load(new FileInputStream("data/jdbc.properties"));
        
        Class.forName(props.getProperty("driver"));
        _con = DriverManager.getConnection(props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
        _dao = new MockDAO(_con);
        setBean(_dao);
    }

    protected void tearDown() throws Exception {
        _con.close();
        super.tearDown();
    }

    public void testQueryTimeout() {
        try {
        	_dao.setQueryTimeout(1);
            _dao.initStatement("DO SLEEP(3)");
        } catch (SQLException se) {
            fail("Cannot prepare statement - " + se.getMessage());
        }
        
        assertEquals(1, _dao.getQueryTimeout());
        validateInput("queryTimeout", new Integer(-1), IllegalArgumentException.class);
        
        // Execute and check for failure
        try {
        	_dao.execute();
        	fail("SQLException expected");
        } catch (SQLException se) {
        	assertEquals(23, se.getErrorCode());
        }
    }
    
    public void testLimits() {
        validateInput("queryMax", new Integer(-1), IllegalArgumentException.class);
        validateInput("queryStart", new Integer(-1), IllegalArgumentException.class);
        _dao.setQueryMax(30);
        _dao.setQueryStart(1);
        assertEquals(30, _dao.getQueryMax());
        assertEquals(1, _dao.getQueryStart());
    }
}