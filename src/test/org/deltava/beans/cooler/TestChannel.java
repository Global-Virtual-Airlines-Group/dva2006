package org.deltava.beans.cooler;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestChannel extends AbstractBeanTestCase {
    
    private Channel _c;
    
    public static Test suite() {
        return new CoverageDecorator(TestChannel.class, new Class[] { Channel.class } );
    }

    protected void setUp() throws Exception {
        super.setUp();
        _c = new Channel("NAME");
        setBean(_c);
    }

    protected void tearDown() throws Exception {
        _c = null;
        super.tearDown();
    }
    
    public void testProperties() {
        assertEquals("NAME", _c.getName());
        checkProperty("description", "DESC");
        checkProperty("lastSubject", "SUBJ");
        checkProperty("viewCount", new Integer(234));
        checkProperty("postCount", new Integer(123));
        checkProperty("threadCount", new Integer(12));
        checkProperty("lastThreadID", new Integer(1234));
        checkProperty("lastThreadID", new Integer(0));
        _c.setActive(true);
        assertTrue(_c.getActive());
        _c.setActive(false);
        assertFalse(_c.getActive());
        assertEquals("NAME", _c.cacheKey());
    }
    
    public void testValidation() {
       validateInput("viewCount", new Integer(-1), IllegalArgumentException.class);
       validateInput("threadCount", new Integer(-1), IllegalArgumentException.class);
       validateInput("postCount", new Integer(-1), IllegalArgumentException.class);
       validateInput("lastThreadID", new Integer(-1), IllegalArgumentException.class);
    }
    
    public void testRoles() {
        assertNotNull(_c.getReadRoles());
        assertEquals(1, _c.getReadRoles().size());
        assertTrue(_c.getReadRoles().contains("*"));
        
        assertNotNull(_c.getWriteRoles());
        assertEquals(1, _c.getWriteRoles().size());
        assertTrue(_c.getWriteRoles().contains("*"));
        
        _c.addRole(false, "Role1");
        assertEquals(1, _c.getReadRoles().size());
        assertTrue(_c.getReadRoles().contains("Role1"));
        _c.addRole(false, "Role2");
        assertEquals(2, _c.getReadRoles().size());
        assertTrue(_c.getReadRoles().contains("Role2"));
        _c.addRole(false, "Role1");
        assertEquals(1, _c.getReadRoles().size());
        
        Set<String> rNames = new HashSet<String>();
        rNames.add("Role3");
        rNames.add("Role4");
        _c.setRoles(true, rNames);
        
        assertEquals(2, _c.getWriteRoles().size());
        assertTrue(_c.getWriteRoles().contains("Role3"));
        assertTrue(_c.getWriteRoles().contains("Role4"));
    }
    
    public void testAirlines() {
        assertNotNull(_c.getAirlines());
        assertEquals(0, _c.getAirlines().size());
        _c.addAirline("dva");
        _c.addAirline("DVA");
        assertEquals(1, _c.getAirlines().size());
        assertTrue(_c.hasAirline("DVA"));
        _c.addAirline("AFV");
        assertEquals(2, _c.getAirlines().size());
        assertTrue(_c.hasAirline("AFV"));
        assertFalse(_c.hasAirline(null));
        
        Set<String> aNames = new HashSet<String>();
        aNames.add("nwa");
        aNames.add("COA");
        aNames.add("coa");
        _c.setAirlines(aNames);
        
        assertEquals(2, _c.getAirlines().size());
        assertTrue(_c.hasAirline("NWA"));
        assertTrue(_c.hasAirline("COA"));
    }
    
    public void testEquality() {
    	assertEquals(_c.getName().hashCode(), _c.hashCode());
    	assertEquals(_c.getName(), _c.toString());
    	
    	Channel c2 = new Channel("NAME");
    	assertFalse(_c == c2);
    	assertTrue(_c.equals(_c.getName()));
    	assertTrue(_c.equals(c2));
    	assertFalse(_c.equals(null));
    	assertFalse(_c.equals(new Object()));
    }
}