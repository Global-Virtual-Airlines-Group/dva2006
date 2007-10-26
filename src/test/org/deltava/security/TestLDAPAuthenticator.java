package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import org.apache.log4j.PropertyConfigurator;

public class TestLDAPAuthenticator extends TestCase {

    private LDAPAuthenticator _auth;
    private Person _usr;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
        _auth = new LDAPAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
        _usr = new AuthPerson("Luke", "Kolin", "cn=Luke Kolin,ou=dva,o=sce");
    }

    protected void tearDown() throws Exception {
        _auth = null;
        _usr = null;
        super.tearDown();
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
    	_usr.setDN("cn=Luke Kolin2,ou=dva,o=sce");
    	assertFalse(_auth.contains(_usr));
    }
    
    public void testAddRemove() throws Exception {
    	Person usr2 = new AuthPerson("Test", "User", "cn=Test User,ou=dva,o=sce");
    	_auth.add(usr2, "test");
    	assertTrue(_auth.contains(usr2));
    	_auth.authenticate(usr2, "test");
    	_auth.remove(usr2);
    }
}