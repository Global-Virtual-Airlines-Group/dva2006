package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;
import org.deltava.beans.navdata.Navaid;
import org.deltava.util.StringUtils;
import org.deltava.util.TaskTimer;

import junit.framework.TestCase;

public class TestDBQuerySpeed extends TestCase {

	private Logger log;
	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common?user=test&password=test";
	
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestDBQuerySpeed.class);
		
		Class<?> c = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testQuery() throws Exception {
		
		TaskTimer ct = new TaskTimer();
		Connection c = DriverManager.getConnection(JDBC_URL);
		assertNotNull(c);
		ct.stop();
		log.info("Connection completed in " + ct.getMillis() + "ms");
		
		List<String> codes = new ArrayList<String>(3600);
		try (PreparedStatement ps = c.prepareStatement("SELECT DISTINCT CODE FROM NAVDATA WHERE (ITEMTYPE=?) OR (ITEMTYPE=?)")) {
			ps.setInt(1, Navaid.VOR.ordinal());
			ps.setInt(2, Navaid.AIRPORT.ordinal());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					codes.add(rs.getString(1));
			}
		}
		
		Collections.shuffle(codes);

		long totalTime = 0; long maxTime = Integer.MIN_VALUE; long minTime = Integer.MAX_VALUE;
		log.info("Executing " + codes.size() + " queries");
		for (String code : codes) {
			TaskTimer tt = new TaskTimer();
			try (PreparedStatement ps = c.prepareStatement("SELECT * FROM NAVDATA WHERE (CODE=?)")) {
				ps.setString(1, code);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						assertNotNull(rs.getString(1));
				}
			}

			tt.stop();
			totalTime += tt.getNanos();
			maxTime = Math.max(maxTime, tt.getNanos());
			minTime = Math.min(minTime, tt.getNanos());
		}
		
		c.close();
		
		// Log execution
		log.info("Max = " + StringUtils.format(maxTime / 1000000d, "#0.000") + " ms");
		log.info("Min = " + StringUtils.format(minTime / 1000000d, "#0.000") + " ms");
		log.info("Avg = " + StringUtils.format(totalTime / 1000000d / codes.size(), "#0.000") + " ms");
	}
}