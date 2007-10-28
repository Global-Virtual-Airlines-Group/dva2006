package org.deltava.jdbc;

import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.*;

import junit.framework.TestCase;

public class TestTransactions extends TestCase {
	
	private Properties _props;
	private ConnectionPool _pool;

	protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("etc/log4j.properties");
        _props = new Properties();
        _props.load(new FileInputStream("data/jdbc.properties"));
        _pool = new ConnectionPool(2);
        _pool.setProperties(_props);
        _pool.setCredentials(_props.getProperty("user"), _props.getProperty("password"));
        _pool.setDriver(_props.getProperty("driver"));
	}

	protected void tearDown() throws Exception {
        _pool.close();
        _props = null;
		LogManager.shutdown();
		super.tearDown();
	}
	
	private void executeSQL(Connection c, String sql) throws SQLException {
		Statement s = c.createStatement();
		s.execute(sql);
		s.close();
	}
	
	private void assertRowCount(int expectedRows, Connection c, String sql) throws SQLException {
		Statement s = c.createStatement();
		ResultSet rs = s.executeQuery(sql);
		int rows = rs.next() ? rs.getInt(1) : 0;
		rs.close();
		s.close();
		assertEquals(expectedRows, rows);
	}

	public void testMultipleRows() throws Exception {
        _pool.connect(1);
        Connection c1 = _pool.getConnection();
        assertNotNull(c1);
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, c1.getTransactionIsolation());
        
        // Clean the table and reset autocommit
        executeSQL(c1, "TRUNCATE test_ids");
        c1.setAutoCommit(false);
        
        // Write row 1
        executeSQL(c1, "INSERT INTO test_ids (ID) VALUES (1)");
        
        // Rollback
        c1.rollback();
        
        // Make sure the row is gone
        assertRowCount(0, c1, "SELECT COUNT(*) FROM test_ids");
        c1.setAutoCommit(true);
        _pool.release(c1);
	}
	
	public void testMultipleTables() throws Exception {
        _pool.connect(2);
        Connection c1 = _pool.getConnection();
        assertNotNull(c1);
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, c1.getTransactionIsolation());

        // Clean the table and reset autocommit
        executeSQL(c1, "TRUNCATE test_ids");
        executeSQL(c1, "TRUNCATE test_ids2");
        c1.setAutoCommit(false);

        // Get a second connection
        Connection c2 = _pool.getConnection();
        assertNotNull(c2);
        assertEquals(Connection.TRANSACTION_REPEATABLE_READ, c2.getTransactionIsolation());
        c2.setAutoCommit(false);
        
        // Write rows on connection 1
        executeSQL(c1, "INSERT INTO test_ids (ID) VALUES (1)");
        executeSQL(c1, "INSERT INTO test_ids2 (ID) VALUES (1)");
        
        // Write rows on connection 2
        executeSQL(c2, "INSERT INTO test_ids (ID) VALUES (2)");
        executeSQL(c2, "INSERT INTO test_ids2 (ID) VALUES (2)");
        
        // Check that c1 cannot see c2's rows
        assertRowCount(1, c1, "SELECT COUNT(*) FROM test_ids");
        assertRowCount(1, c1, "SELECT COUNT(*) FROM test_ids2");
        
        // Check that c2 cannot see c1's rows
        assertRowCount(1, c2, "SELECT COUNT(*) FROM test_ids");
        assertRowCount(1, c2, "SELECT COUNT(*) FROM test_ids2");

        // Rollback c2
        c2.rollback();
        c2.setAutoCommit(true);
        
        // Check that c2 cannot see c1's rows
        assertRowCount(0, c2, "SELECT COUNT(*) FROM test_ids");
        assertRowCount(0, c2, "SELECT COUNT(*) FROM test_ids2");

        // Commit c1
        c1.commit();
        c1.setAutoCommit(true);
        
        // Check that c1 and c2 can see c1's rows
        assertRowCount(1, c2, "SELECT COUNT(*) FROM test_ids");
        assertRowCount(1, c2, "SELECT COUNT(*) FROM test_ids2");
        assertRowCount(1, c1, "SELECT COUNT(*) FROM test_ids");
        assertRowCount(1, c1, "SELECT COUNT(*) FROM test_ids2");

        // Clean up and close
        _pool.release(c2);
        executeSQL(c1, "TRUNCATE test_ids");
        executeSQL(c1, "TRUNCATE test_ids2");
        _pool.release(c1);
	}
}