// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.*;
import org.deltava.util.StringUtils;

import junit.framework.TestCase;

public class CountryLoader extends TestCase {
	
	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://polaris.sce.net/common";
	
	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(RunwayLoader.class);
		
		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadCountryCodes() throws SQLException, IOException {
		
		File f = new File("c:\\temp\\iso-3316-2.csv");
		assertTrue(f.exists());
		
		Map<String, String> codes = new TreeMap<String, String>();
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		lr.readLine();
		while (lr.ready()) {
			String data = lr.readLine();
			String code = data.substring(0, data.indexOf(',')).toUpperCase();
			if (codes.containsKey(code))
				continue;
			
			// Get the country name
			int pos = data.indexOf(',', code.length() + 2) + 1;
			char fc = data.charAt(pos);
			String name;
			if (fc == '\"') {
				name = data.substring(pos + 1, data.indexOf('\"', pos + 1));
				List<String> pts = StringUtils.split(name, ",");
				Collections.reverse(pts);
				name = StringUtils.listConcat(pts, " ").trim();
			} else
				name = data.substring(pos, data.indexOf(',', pos + 1));
			
			// Format stuff
			name = StringUtils.properCase(name);
			name = name.replace(" The", " the");
			name = name.replace(" And", " and");
			name = name.replace(" Of", " of");
			name = name.replace("\'S ", " \'s ");
			
			codes.put(code, name);
			log.info(code + " = " + name);
		}
		
		lr.close();
		
		// Clear the table
		Statement s = _c.createStatement();
		s.executeUpdate("DELETE FROM COUNTRY");
		s.close();
		
		// Write to the database
		PreparedStatement ps = _c.prepareStatement("REPLACE INTO COUNTRY (CODE, NAME) VALUES (?,?)");
		for (Iterator<Map.Entry<String, String>> i = codes.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, String> me = i.next();
			ps.setString(1, me.getKey());
			ps.setString(2, me.getValue());
			ps.addBatch();
		}
		
		// Execute and commit
		ps.executeBatch();
		_c.commit();
		ps.close();
	}
}