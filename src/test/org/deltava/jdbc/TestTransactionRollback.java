// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.jdbc;

import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

public class TestTransactionRollback extends TestCase {
	
	private final Properties _props = new Properties();
	private Connection _c;

	protected void setUp() throws Exception {
		super.setUp();
		
		_props.load(new FileInputStream("data/jdbc.properties"));
		assertTrue(_props.containsKey("url"));
        assertTrue(_props.containsKey("driver"));
        Class.forName(_props.getProperty("driver"));
	}

	protected void tearDown() throws Exception {
		Statement s = _c.createStatement();
		s.execute("DROP TABLE IDS");
		s.close();
		
		_c.close();
		_c = null;
		_props.clear();
		super.tearDown();
	}
	
	private void initTable() throws SQLException {
		Statement s = _c.createStatement();
		s.execute("CREATE TABLE IDS ( ID INTEGER UNSIGNED NOT NULL, PRIMARY KEY(ID)) Engine=INNODB");
		s.execute("INSERT INTO IDS VALUES (1)");
		s.close();
	}
	
	private int getRowCount() throws SQLException {
		Statement s = _c.createStatement();
		ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM IDS");
		int rows = rs.next() ? rs.getInt(1) : -1;
		rs.close();
		s.close();
		return rows;
	}
	
	private void throwError() {
		try {
			Statement s = _c.createStatement();
			s.execute("INSERT INTO IDS VALUES(1)");
			s.close();
			fail("No Error Thrown");
		} catch (Exception e) {
			// empty
		}
	}
	
	public void testLocalSessionState() throws Exception {
		_props.setProperty("useLocalSessionState", "true");
		_c = DriverManager.getConnection(_props.getProperty("url"), _props);
		_c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		assertEquals(Connection.TRANSACTION_READ_COMMITTED, _c.getTransactionIsolation());
		
		// Create the table
		initTable();
		
		// Throw an error
		throwError();
		
		// Rollback
		_c.rollback();
		
		// Get rows
		assertEquals(0, getRowCount());
		
		// Commit
		_c.commit();
		
		// Get rows
		assertEquals(0, getRowCount());
		
		// Set auto-commit = true
		_c.setAutoCommit(true);
		assertTrue(_c.getAutoCommit());
		
		// Get rows
		assertEquals(0, getRowCount());
	}
	
	public void testServerSessionState() throws Exception {
		_props.setProperty("useLocalSessionState", "false");
		_c = DriverManager.getConnection(_props.getProperty("url"), _props);
		_c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		assertEquals(Connection.TRANSACTION_READ_COMMITTED, _c.getTransactionIsolation());
		
		// Create the table
		initTable();
		
		// Throw an error
		throwError();

		// Rollback
		_c.rollback();
		
		// Get rows
		assertEquals(0, getRowCount());
		
		// Commit
		_c.commit();
		
		// Get rows
		assertEquals(0, getRowCount());

		// Set auto-commit = true
		_c.setAutoCommit(true);
		assertTrue(_c.getAutoCommit());
		
		// Get rows
		assertEquals(0, getRowCount());
	}
}