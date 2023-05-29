package org.deltava.security;

import junit.framework.TestCase;

import org.deltava.beans.Person;

import java.io.File;

public class TestFileAuthenticator extends TestCase {

    private Authenticator _auth;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
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
        } catch (SecurityException se) {
        	// empty
        }
    }
}