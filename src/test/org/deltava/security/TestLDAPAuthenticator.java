package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import java.io.File;

public class TestLDAPAuthenticator extends TestCase {

    private LDAPAuthenticator _auth;
    private Person _usr;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
        _auth = new LDAPAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
        _usr = new AuthPerson("Luke", "Kolin", "cn=Luke Kolin,ou=dva,o=gva");
    }

    public void testAuthentication() {
    	_auth.authenticate(_usr, "maddog");
        try {
        	_auth.authenticate(_usr, "bad_password");
            fail("SecurityException expected");
        } catch (SecurityException se) {
            return;
        }
    }
    
    public void testSearch() throws Exception {
    	assertTrue(_auth.contains(_usr));
    	_usr.setDN("cn=Luke Kolin2,ou=dva,o=gva");
    	assertFalse(_auth.contains(_usr));
    }
    
    public void testAddRemove() throws Exception {
    	Person usr2 = new AuthPerson("Test", "User", "cn=Test User,ou=dva,o=gva");
    	_auth.add(usr2, "test");
    	assertTrue(_auth.contains(usr2));
    	_auth.authenticate(usr2, "test");
    	_auth.remove(usr2);
    }
}