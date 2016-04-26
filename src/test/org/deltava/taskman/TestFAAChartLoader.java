// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taskman;

import java.sql.*;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;

import org.deltava.tasks.FAAChartLoaderTask;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.ConnectionPool;

public class TestFAAChartLoader extends TestCase {
	
	final class MockFAALoaderTask extends FAAChartLoaderTask {
		
		public void exec(TaskContext ctx) {
			execute(ctx);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");

		// Init SystemData
		SystemData.init();
		SystemData.add("airline.code", "DVA");
		SystemData.add("schedule.chart.url.faa", "file:///C:/users/luke/desktop");
		
		// Initialize the connection pool
		ConnectionPool jdbcPool = new ConnectionPool(10, "DVA");
		jdbcPool.setProperties((Map<?, ?>) SystemData.getObject("jdbc.connectProperties"));
		jdbcPool.setCredentials("luke", "14072");
		jdbcPool.setURL(SystemData.get("jdbc.url"));
		jdbcPool.setDriver(SystemData.get("jdbc.driver"));
		jdbcPool.connect(SystemData.getInt("jdbc.pool_size"));
		SystemData.add(SystemData.JDBC_POOL, jdbcPool);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		Connection c = jdbcPool.getConnection();
		assertNotNull(c);

		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(c);
		tzdao.initAll();
		GetCountry cdao = new GetCountry(c);
		cdao.initAll();
		GetAirline aldao = new GetAirline(c);
		SystemData.add("airlines", aldao.getAll());
		GetAirport apdao = new GetAirport(c);
		Map<String, Airport> airports = apdao.getAll(); 
		SystemData.add("airports", airports);
		
		jdbcPool.release(c);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadMetadata() {
		
		TaskContext ctx = new TaskContext();
		assertNotNull(ctx);
		
		MockFAALoaderTask task = new MockFAALoaderTask();
		assertNotNull(task);
		task.exec(ctx);
	}
}