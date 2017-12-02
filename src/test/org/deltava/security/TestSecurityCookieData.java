package org.deltava.security;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestSecurityCookieData extends AbstractBeanTestCase {

    private SecurityCookieData _cData;
    
    public static Test suite() {
        return new CoverageDecorator(TestSecurityCookieData.class, new Class[] { SecurityCookieData.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _cData = new SecurityCookieData("cn=Luke,ou=dva,o=sce");
        setBean(_cData);
    }

    @Override
	protected void tearDown() throws Exception {
        _cData = null;
        super.tearDown();
    }

    public void testProperties() throws InterruptedException {
        assertEquals("cn=Luke,ou=dva,o=sce", _cData.getUserID());
        assertFalse(_cData.isExpired());
        checkProperty("expiryDate", Long.valueOf(System.currentTimeMillis()));
        checkProperty("loginDate", Long.valueOf(System.currentTimeMillis()));
        checkProperty("remoteAddr", "127.0.0.1");
        Thread.sleep(100);
        assertTrue(_cData.isExpired());
    }
}