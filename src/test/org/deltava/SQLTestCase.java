// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.StringUtils;
import org.deltava.util.system.*;

public abstract class SQLTestCase extends TestCase {

	private ConnectionPool _jdbcPool;

	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");

		// Load SystemData object
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);

		// Init the connection Pool
		_jdbcPool = new ConnectionPool(1);
		_jdbcPool.setCredentials("sa", "");
		_jdbcPool.setProperty("url", "jdbc:hsqldb:mem:test");
		_jdbcPool.setDriver("org.hsqldb.jdbcDriver");
		_jdbcPool.connect(1);
		SystemData.add(SystemData.JDBC_POOL, _jdbcPool);
	}

	protected Connection getHSQLConnection() throws SQLException {
		try {
			return _jdbcPool.getConnection();
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	protected void closeConnection(Connection c) {
		try {
			c.close();
		} catch (Exception e) {
			// nothing
		}
	}

	/**
	 * Loads a DDL file and executes it.
	 */
	protected void createTable(String ddlFile) throws IOException, SQLException {

		// Load the File
		StringBuilder buf = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(ddlFile));
		while (br.ready()) {
			buf.append(br.readLine());
			buf.append("\n");
		}

		// Close the file
		br.close();

		// Execute the DDL
		Connection c = getHSQLConnection();
		PreparedStatement ps = c.prepareStatement(buf.toString());
		ps.executeUpdate();
		ps.close();
		_jdbcPool.release(c);
	}

	protected void executeSQL(String sql) throws SQLException {
		Connection c = getHSQLConnection();
		Statement s = c.createStatement();
		s.execute(sql);
		s.close();
		_jdbcPool.release(c);
	}

	protected void insertRow(String table, String csvEntry) throws SQLException {
		Connection c = getHSQLConnection();
		insertRow(c, table, csvEntry);
		closeConnection(c);
	}

	protected void insertRow(Connection c, String table, String csvEntry) throws SQLException {
		StringBuilder buf = new StringBuilder("INSERT INTO ");
		buf.append(table);
		buf.append(" VALUES (");

		// Get the values
		List<String> values = StringUtils.split(csvEntry, ",");
		for (int x = 1; x <= values.size(); x++) {
			buf.append('?');
			if (x < values.size())
				buf.append(',');
		}

		buf.append(')');

		// Prepare the statement
		PreparedStatement ps = c.prepareStatement(buf.toString());
		for (int x = 1; x <= values.size(); x++)
			ps.setString(x, values.get(x));

		// Execute the statement and clean up
		ps.executeUpdate();
		ps.close();
	}
}