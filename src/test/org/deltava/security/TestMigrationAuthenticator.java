package org.deltava.security;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class TestMigrationAuthenticator extends TestCase {

    private MigrationAuthenticator _auth;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
        _auth = new MigrationAuthenticator();
        _auth.init(Authenticator.DEFAULT_PROPS_FILE);
    }

    protected void tearDown() throws Exception {
        _auth = null;
        super.tearDown();
    }
    
    public void testSrcAuthenticationAndCopy() {
    	assertFalse(_auth.getDestination().contains("cn=Luke Kolin,ou=afv,o=sce"));
    	_auth.authenticate("cn=Luke Kolin,ou=afv,o=sce", "maddog");
    	assertTrue(_auth.getDestination().contains("cn=Luke Kolin,ou=afv,o=sce"));
    	_auth.removeUser("cn=Luke Kolin,ou=afv,o=sce");
    	assertFalse(_auth.getDestination().contains("cn=Luke Kolin,ou=afv,o=sce"));
    }
    
    public void testAddRemove() throws Exception {
    	_auth.addUser("cn=Test User,ou=dva,o=sce", "test");
    	assertTrue(_auth.contains("cn=Test User,ou=dva,o=sce"));
    	assertTrue(_auth.getDestination().contains("cn=Test User,ou=dva,o=sce"));
    	assertFalse(_auth.getSource().contains("cn=Test User,ou=dva,o=sce"));
    	_auth.authenticate("cn=Test User,ou=dva,o=sce", "test");
    	_auth.removeUser("cn=Test User,ou=dva,o=sce");
    }
}