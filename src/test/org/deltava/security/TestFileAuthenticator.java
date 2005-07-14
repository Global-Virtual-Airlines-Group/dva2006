package org.deltava.security;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class TestFileAuthenticator extends TestCase {

    private Authenticator _auth;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("etc/log4j.properties");
    }
    
    protected void tearDown() throws Exception {
        _auth = null;
        super.tearDown();
    }
    
    public void testAuthentication() {
        _auth = new FileAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
        assertNotNull(_auth);
        
        // Test authentication
        _auth.authenticate("cn=Terry Eshenour,ou=dva,o=sce", "terry");
        
        // Test bad password exception - this also tests case sensitivity
        try {
            _auth.authenticate("cn=David Schaum,ou=dva,o=sce", "David");
            fail("SecurityException expected");
        } catch (SecurityException se) { }
    }
}