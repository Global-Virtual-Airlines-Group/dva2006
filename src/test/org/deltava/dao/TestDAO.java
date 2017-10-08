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
    
    private class UpdateDAO extends DAO {
    	
    	public UpdateDAO(Connection c) {
            super(c);
        }
    	
    	public void testExecuteUpdate(int id, int v, int expected) throws SQLException {
    		prepareStatement("INSERT INTO UPD_TEST VALUES (?, ?)");
  			_ps.setInt(1, id);
   			_ps.setInt(2, v);
   			executeUpdate(expected);
    	}
    	
    	public void testExecuteBatchUpdate(int[] ids, int expected) throws SQLException {
    		prepareStatement("INSERT INTO UPD_TEST VALUES (?, ?)");
    		for (int x = 0; x < ids.length; x++) {
    			_ps.setInt(1, ids[x]);
    			_ps.setInt(2, x);
    			_ps.addBatch();
    		}
    		
    		executeBatchUpdate(0, expected);
    	}
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        
        Properties props = new Properties();
        props.load(new FileInputStream("data/jdbc.properties"));
        
        Class.forName(props.getProperty("driver"));
        _con = DriverManager.getConnection(props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
        _dao = new MockDAO(_con);
        setBean(_dao);
    }

    @Override
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
        
        // Execute and check for failure
        try {
        	_dao.execute();
        	fail("SQLException expected");
        } catch (SQLException se) {
        	assertEquals(0, se.getErrorCode());
        }
    }
    
    public void testLimits() {
        _dao.setQueryMax(30);
        _dao.setQueryStart(1);
        assertEquals(30, _dao.getQueryMax());
        assertEquals(1, _dao.getQueryStart());
    }
    
    public void testExpectedResults() throws SQLException{

    	// Create the temp table
		try (Statement s = _con.createStatement()) {
			s.executeUpdate("CREATE TEMPORARY TABLE UPD_TEST ( ID INTEGER UNSIGNED NOT NULL, V INTEGER UNSIGNED NOT NULL, PRIMARY KEY (ID) )");
		}
    	
    	UpdateDAO upddao = new UpdateDAO(_con);
    	upddao.testExecuteUpdate(31, 0, 1);
    	try {
    		upddao.testExecuteUpdate(33, 0, 3);
    		fail("SQLException expected");
    	} catch (SQLException se) {
    		// yay!
    	}
    	
    	upddao.testExecuteBatchUpdate(new int[] { 25,  26, 27}, 3);
    	upddao.testExecuteBatchUpdate(new int[] { 125,  126, 127}, 1);
    	upddao.testExecuteBatchUpdate(new int[] {}, 0);
    	try {
    		upddao.testExecuteBatchUpdate(new int[] { 225,  226, 227}, 4);
    		fail("SQLException expected");
    	} catch (SQLException se) {
    		//yay
    	}
    }
}