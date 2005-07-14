package org.deltava.security;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class TestLDAPAuthenticator extends TestCase {

    private LDAPAuthenticator _auth;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
        _auth = new LDAPAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
    }

    protected void tearDown() throws Exception {
        _auth = null;
        super.tearDown();
    }
    
    public void testAuthentication() {
        _auth.authenticate("cn=Luke Kolin, ou=dva, o=sce", "maddog");
        try {
            _auth.authenticate("cn=Luke Kolin, ou=dva, o=sce", "bad_password");
            fail("SecurityException expected");
        } catch (SecurityException se) {
            return;
        }
    }
    
    public void testSearch() throws Exception {
    	assertTrue(_auth.contains("cn=Luke Kolin,ou=dva,o=sce"));
    	assertFalse(_auth.contains("cn=Luke Kolin2,ou=dva,o=sce"));
    }
    
    public void testAddRemove() throws Exception {
    	_auth.addUser("cn=Test User,ou=dva,o=sce", "test");
    	assertTrue(_auth.contains("cn=Test User,ou=dva,o=sce"));
    	_auth.authenticate("cn=Test User,ou=dva,o=sce", "test");
    	_auth.removeUser("cn=Test User,ou=dva,o=sce");
    }
}