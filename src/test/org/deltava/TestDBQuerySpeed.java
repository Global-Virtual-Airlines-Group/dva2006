package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;
import org.deltava.beans.navdata.Navaid;
import org.deltava.util.*;

import junit.framework.TestCase;

public class TestDBQuerySpeed extends TestCase {

	private Logger log;
	
	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common?user=test&password=test";
	
	private static final int THREADS = 8;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("data/log4j.test.properties");
		log = Logger.getLogger(TestDBQuerySpeed.class);
		
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        System.setProperties(systemProperties);
		
		Class<?> c = Class.forName("com.mysql.cj.jdbc.Driver");
		assertNotNull(c);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	private abstract class TimedWorker extends Thread {
		protected final List<String> _codes = new ArrayList<String>();
		protected long totalTime;
		protected long maxTime = Integer.MIN_VALUE;
		protected long minTime = Integer.MAX_VALUE;
		
		protected TimedWorker(String name, Collection<String> codes) {
			super(name);
			setDaemon(true);
			_codes.addAll(codes);
			Collections.shuffle(_codes);
		}

		public double getMax() {
			return maxTime / 1000000d;
		}
		
		public double getMin() {
			return minTime / 1000000d;
		}
		
		public double getAverage() {
			return totalTime / 1000000d / _codes.size();
		}
	}
	
	private class QueryWorker extends TimedWorker {
		QueryWorker(int id, Collection<String> codes) {
			super("DBWorker-" + String.valueOf(id), codes);
		}
		
		@Override
		public void run() {
			
			try (Connection c = DriverManager.getConnection(JDBC_URL)) {
				TaskTimer tt = new TaskTimer();
				for (String code : _codes) {
					tt.start();
					try (PreparedStatement ps = c.prepareStatement("SELECT * FROM NAVDATA WHERE (CODE=?)")) {
						ps.setString(1, code);
						ps.setFetchSize(32);
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
			} catch (SQLException se) {
				throw new RuntimeException(se);
			}
		}
	}
	
	public void testDBQuery() throws Exception {
		
		List<String> codes = new ArrayList<String>(16384);
		TaskTimer ct = new TaskTimer();
		try (Connection c = DriverManager.getConnection(JDBC_URL)) {
			ct.stop();
			log.info("Connection completed in " + ct.getMillis() + "ms");
		
			try (PreparedStatement ps = c.prepareStatement("SELECT DISTINCT CODE FROM NAVDATA WHERE (ITEMTYPE=?) OR (ITEMTYPE=?)")) {
				ps.setInt(1, Navaid.VOR.ordinal());
				ps.setInt(2, Navaid.AIRPORT.ordinal());
				ps.setFetchSize(500);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next())
						codes.add(rs.getString(1));
				}
			}
		}
		
		log.info("Executing " + codes.size() + " queries using " + THREADS + " threads");
		Collection<TimedWorker> workers = new ArrayList<TimedWorker>();
		for (int x = 1; x <= THREADS; x++) {
			QueryWorker wrk = new QueryWorker(x, codes);
			workers.add(wrk);
			wrk.start();
		}
		
		ThreadUtils.waitOnPool(workers);
		
		// Log execution
		for (TimedWorker wrk : workers) {
			log.info(wrk.getName());
			log.info("Max = " + StringUtils.format(wrk.getMax(), "#0.000") + " ms");
			log.info("Min = " + StringUtils.format(wrk.getMin(), "#0.000") + " ms");
			log.info("Avg = " + StringUtils.format(wrk.getAverage(), "#0.000") + " ms");
		}
	}
}