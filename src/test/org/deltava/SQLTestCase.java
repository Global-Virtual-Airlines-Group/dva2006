package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.gvagroup.pool.JDBCPool;

import org.deltava.util.StringUtils;
import org.deltava.util.system.*;

public abstract class SQLTestCase extends TestCase {

	private JDBCPool _jdbcPool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());

		// Load SystemData object
		SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
		
		// Load database properties
        Properties props = new Properties();
        props.load(new FileInputStream("data/jdbc.properties"));

		// Init the connection Pool
		_jdbcPool = new JDBCPool(1, "test");
        _jdbcPool.setProperties(props);
        _jdbcPool.setCredentials(props.getProperty("user"), props.getProperty("password"));
        _jdbcPool.setDriver(props.getProperty("driver"));
		_jdbcPool.connect(1);
		SystemData.add(SystemData.JDBC_POOL, _jdbcPool);
		
		// Create the database
		executeSQL("DROP DATABASE IF EXISTS test");
		executeSQL("CREATE DATABASE IF NOT EXISTS test");
		executeSQL("USE test");
	}
	
	@Override
	protected void tearDown() throws Exception {
		executeSQL("DROP DATABASE IF EXISTS test");
		executeSQL("CREATE DATABASE IF NOT EXISTS test");
		_jdbcPool.close();
		super.tearDown();
	}

	protected Connection getSQLConnection() throws SQLException {
		try {
			return _jdbcPool.getConnection();
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * Loads a DDL file and executes it.
	 */
	@SuppressWarnings("javadoc")
	protected void createTable(String ddlFile) throws IOException, SQLException {

		// Load the File
		StringBuilder buf = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(ddlFile))) {
			while (br.ready()) {
				buf.append(br.readLine());
				buf.append('\n');
			}
		}

		// Execute the DDL
		Connection c = getSQLConnection();
		try (PreparedStatement ps = c.prepareStatement(buf.toString())) {
			ps.executeUpdate();
		}
		
		returnConnection(c);
	}

	protected void executeSQL(String sql) throws SQLException {
		Connection c = getSQLConnection();
		try (Statement s = c.createStatement()) {
			s.execute(sql);
		}

		returnConnection(c);
	}

	protected void insertRow(String table, String csvEntry) throws SQLException {
		Connection c = getSQLConnection();
		insertRow(c, table, csvEntry);
		returnConnection(c);
	}
	
	protected void returnConnection(Connection c) {
		_jdbcPool.release(c);
	}

	protected static void insertRow(Connection c, String table, String csvEntry) throws SQLException {
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
		try (PreparedStatement ps = c.prepareStatement(buf.toString())) {
			for (int x = 1; x <= values.size(); x++)
				ps.setString(x, values.get(x - 1));

			ps.executeUpdate();
		}
	}
}