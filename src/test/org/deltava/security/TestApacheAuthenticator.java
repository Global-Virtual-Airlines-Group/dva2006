package org.deltava.security;

import org.deltava.SQLTestCase;

public class TestApacheAuthenticator extends SQLTestCase {
	
	private Authenticator _auth;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Create the authenticator
		_auth = new ApacheAuthenticator();
		assertNotNull(_auth);
		
		// Add a dummy Airline record
		createTable("sql/system/create_airline_info.sql");
		insertRow("AIRLINEINFO", "DVA,Delta Virtual,dva,deltava.org,FALSE");
		
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
		usr.setID(1);
		
		// Add a user
		assertFalse(_auth.contains(usr));
		_auth.addUser(usr, "password");
		assertTrue(_auth.contains(usr));
	}
}