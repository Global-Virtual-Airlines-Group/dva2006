package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import org.apache.log4j.PropertyConfigurator;

public class TestDominoAuthenticator extends TestCase {

    private DominoAuthenticator _auth;
    private Person _usr;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
        _auth = new DominoAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
        _usr = new AuthPerson("Luke", "Kolin", "cn=Luke Kolin, ou=dva, o=sce");
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
    	_auth.authenticate(_usr, "maddog");
    	assertTrue(_auth.contains("cn=Luke Kolin,ou=dva,o=sce"));
    }
}