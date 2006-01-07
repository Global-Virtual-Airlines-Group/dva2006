package org.deltava.security;

import org.deltava.beans.Person;

import org.apache.log4j.PropertyConfigurator;

public class TestLDAPAuthenticator extends AuthenticatorTestCase {

    private LDAPAuthenticator _auth;
    private Person _usr;
    
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
    	_auth.authenticate(_usr, "maddog");
        try {
        	_auth.authenticate(_usr, "bad_password");
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
    	Person usr2 = new AuthPerson("Test", "User", "cn=Test User,ou=dva,o=sce");
    	_auth.addUser(usr2, "test");
    	assertTrue(_auth.contains(usr2.getDN()));
    	_auth.authenticate(usr2, "test");
    	_auth.removeUser(usr2.getDN());
    }
}