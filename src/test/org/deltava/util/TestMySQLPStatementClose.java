package org.deltava.util;

import java.sql.*;
import java.util.Properties;

import junit.framework.TestCase;

public class TestMySQLPStatementClose extends TestCase {

	private Connection _c;

	private static final String URL = "jdbc:mysql://polaris.sce.net/acars?user=luke&password=test";

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Connect to the database
		final Class<?> drv = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(drv);

		final Properties p = new Properties();
		p.setProperty("useServerPrepStmts", "true");
		p.setProperty("cachePrepStmts", "true");

		_c = DriverManager.getConnection(URL, p);
		assertNotNull(_c);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	public void testDuplicateClose() throws SQLException {

		PreparedStatement ps = _c.prepareStatement("select now()");
		assertNotNull(ps);

		try (ResultSet rs = ps.executeQuery()) {
			assertNotNull(rs);
			assertTrue(rs.next());
			assertNotNull(rs.getString(1));
			assertFalse(rs.next());
		}
		
		assertFalse(ps.isClosed());
		ps.close();
		assertTrue(ps.isClosed());
		assertNotNull(ps.getConnection());
		ps.close();
		assertTrue(ps.isClosed());
		try {
			assertNull(ps.getConnection());
			fail("Should throw exception");
		} catch (SQLException se) { /* empty */ }
		
		PreparedStatement ps2 = _c.prepareStatement("select curdate()");
		assertNotNull(ps2);
		try (ResultSet rs = ps2.executeQuery()) {
			assertNotNull(rs);
			assertTrue(rs.next());
			assertNotNull(rs.getString(1));
			assertFalse(rs.next());
		}

		assertFalse(ps2.isClosed());
		ps2.close();
		assertTrue(ps2.isClosed());
		
		PreparedStatement ps3 = _c.prepareStatement("select curdate()");
		assertNotNull(ps3);
		assertSame(ps2, ps3);
		assertFalse(ps3.isClosed());
		try (ResultSet rs = ps3.executeQuery()) {
			assertNotNull(rs);
			assertTrue(rs.next());
			assertNotNull(rs.getString(1));
			assertFalse(rs.next());
		}

		ps3.close();
		assertTrue(ps3.isClosed());
		assertNotNull(ps3.getConnection());
	}
}