package org.deltava.security;

import org.deltava.beans.Person;

import org.apache.log4j.PropertyConfigurator;

public class TestMigrationAuthenticator extends AuthenticatorTestCase {

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
    	Person usr2 = new AuthPerson("Luke", "Kolin", "cn=Luke Kolin,ou=afv,o=sce");
    	assertFalse(_auth.getDestination().contains(usr2.getDN()));
    	_auth.authenticate(usr2, "maddog");
    	assertTrue(_auth.getDestination().contains(usr2.getDN()));
    	_auth.removeUser(usr2.getDN());
    	assertFalse(_auth.getDestination().contains(usr2.getDN()));
    }
    
    public void testAddRemove() throws Exception {
    	Person usr2 = new AuthPerson("Test", "User", "cn=Test User,ou=dva,o=sce");
    	_auth.addUser(usr2, "test");
    	assertTrue(_auth.contains(usr2.getDN()));
    	assertTrue(_auth.getDestination().contains(usr2.getDN()));
    	assertFalse(_auth.getSource().contains(usr2.getDN()));
    	_auth.authenticate(usr2, "test");
    	_auth.removeUser(usr2.getDN());
    }
}