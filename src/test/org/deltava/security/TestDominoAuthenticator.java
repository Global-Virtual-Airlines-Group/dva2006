package org.deltava.security;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class TestDominoAuthenticator extends TestCase {

    private DominoAuthenticator _auth;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
        _auth = new DominoAuthenticator();
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
    	_auth.authenticate("cn=Luke Kolin,ou=dva,o=sce", "maddog");
    	assertTrue(_auth.contains("cn=Luke Kolin,ou=dva,o=sce"));
    }
}