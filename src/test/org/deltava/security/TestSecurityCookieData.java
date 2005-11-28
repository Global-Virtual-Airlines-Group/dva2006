package org.deltava.security;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;


public class TestSecurityCookieData extends AbstractBeanTestCase {

    private SecurityCookieData _cData;
    
    public static Test suite() {
        return new CoverageDecorator(TestSecurityCookieData.class, new Class[] { SecurityCookieData.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _cData = new SecurityCookieData("cn=Luke,ou=dva,o=sce");
        setBean(_cData);
    }

    protected void tearDown() throws Exception {
        _cData = null;
        super.tearDown();
    }

    public void testProperties() throws InterruptedException {
        assertEquals("cn=Luke,ou=dva,o=sce", _cData.getUserID());
        assertFalse(_cData.isExpired());
        checkProperty("expiryDate", new Long(System.currentTimeMillis()));
        checkProperty("remoteAddr", "127.0.0.1");
        checkProperty("password", "testPassword");
        assertEquals("7465737450617373776f7264", _cData.getPasswordBytes());
        Thread.sleep(100);
        assertTrue(_cData.isExpired());
    }
    
    public void testMapConstructor() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("uid", "cn=Luke,ou=dva,o=sce");
        data.put("addr", "127.0.0.1");
        
        SecurityCookieData d2 = new SecurityCookieData(data);
        assertEquals("cn=Luke,ou=dva,o=sce", d2.getUserID());
        assertEquals("127.0.0.1", d2.getRemoteAddr());
        assertNull(d2.getPassword());
    }
    
    public void testValidation() {
        validateInput("expiryDate", new Long(-1), IllegalArgumentException.class);
    }
}
