package org.deltava.security;

import java.sql.Connection;

import org.deltava.SQLTestCase;

public class TestApacheSQLAuthenticator extends SQLTestCase {
	
	private SQLAuthenticator _auth;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Create the authenticator
		_auth = new ApacheSQLAuthenticator();
		assertNotNull(_auth);
		
		// Add a dummy Airline record
		createTable("sql/system/create_airline_info.sql");
		insertRow("AIRLINEINFO", "DVA,Delta Virtual,dva,deltava.org,0");
		
		// Add a dummy ID
		createTable("sql/system/create_userdata.sql");
		insertRow("USERDATA", "8027,DVA,PILOTS");
		
		// Build the tables
		createTable("sql/system/create_auth.sql");
		createTable("sql/system/create_auth_alias.sql");
	}

	protected void tearDown() throws Exception {
		executeSQL("DROP TABLE AUTH_ALIAS");
		executeSQL("DROP TABLE AUTH");
		super.tearDown();
	}

	public void testBasicUser() throws Exception {
		
		// Create a user
		AuthPerson usr = new AuthPerson("Luke", "Kolin", "luke");
		usr.setID(8027);
		Connection c = getSQLConnection();
		assertNotNull(c);
		_auth.setConnection(c);
		
		// Add a user
		assertFalse(_auth.contains(usr));
		_auth.add(usr, "password");
		assertTrue(_auth.contains(usr));
		_auth.authenticate(usr, "password");
		_auth.clearConnection();
		returnConnection(c);
	}
}