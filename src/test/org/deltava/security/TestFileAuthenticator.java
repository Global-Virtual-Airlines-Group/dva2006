package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import org.apache.log4j.PropertyConfigurator;

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
        Person usr2 = new AuthPerson("Terry", "Eshenour", "cn=Terry Eshenour,ou=dva,o=sce");
        _auth.authenticate(usr2, "terry");
        
        // Test bad password exception - this also tests case sensitivity
        try {
        	usr2 = new AuthPerson("David", "Schaum", "cn=David Schaum,ou=dva,o=sce");
            _auth.authenticate(usr2, "David");
            fail("SecurityException expected");
        } catch (SecurityException se) { }
    }
}