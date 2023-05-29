package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import java.io.File;

public class TestMigrationAuthenticator extends TestCase {

	private MigrationAuthenticator _auth;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		_auth = new MigrationAuthenticator();
		_auth.init(Authenticator.DEFAULT_PROPS_FILE);
	}

	public void testSrcAuthenticationAndCopy() {
		Person usr2 = new AuthPerson("Luke", "Kolin", "cn=Luke Kolin,ou=afv,o=sce");
		assertFalse(_auth.getDestination().isEmpty());
		Authenticator dst = _auth.getDestination().iterator().next();
		assertFalse(dst.contains(usr2));
		_auth.authenticate(usr2, "maddog");
		assertTrue(dst.contains(usr2));
		_auth.remove(usr2);
		assertFalse(dst.contains(usr2));
	}

	public void testAddRemove() throws Exception {
		assertFalse(_auth.getDestination().isEmpty());
		Person usr2 = new AuthPerson("Test", "User", "cn=Test User,ou=dva,o=sce");
		_auth.add(usr2, "test");
		assertTrue(_auth.contains(usr2));
		assertTrue(_auth.getDestination().iterator().next().contains(usr2));
		assertFalse(_auth.getSource().contains(usr2));
		_auth.authenticate(usr2, "test");
		_auth.remove(usr2);
	}
}