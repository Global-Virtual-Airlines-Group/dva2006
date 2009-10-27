package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import org.deltava.beans.fleet.Resource;

import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class LoadResourceDomain extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";

	private Connection _c;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		SystemData.init();

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "import", "import");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadLocations() throws Exception {

		// Get the resources
		GetResources rdao = new GetResources(_c);
		SetResource rwdao = new SetResource(_c);
		Collection<Resource> rsrcs = rdao.getAll(null, "CREATEDON");
		for (Iterator<Resource> i = rsrcs.iterator(); i.hasNext(); ) {
			Resource r = i.next();
			rwdao.write(r);
		}
		
		// Commit
		_c.commit();
	}
}