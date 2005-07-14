package org.deltava.beans.schedule;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestOceanicRoute extends AbstractBeanTestCase {

    private OceanicRoute _or;
    
    public static Test suite() {
        return new CoverageDecorator(TestOceanicRoute.class, new Class[] { OceanicRoute.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _or = new OceanicRoute(OceanicRoute.NAT);
        setBean(_or);
    }

    protected void tearDown() throws Exception {
        _or = null;
        super.tearDown();
    }
    
    public void testProperties() {
        assertEquals(OceanicRoute.NAT, _or.getType());
        assertEquals(OceanicRoute.TYPES[OceanicRoute.NAT], _or.getTypeName());
        checkProperty("date", new Date());
        checkProperty("source", "localhost");
        checkProperty("route", "!@# SADJSAKD");
        checkProperty("type", new Integer(OceanicRoute.PACOT));
        assertEquals(OceanicRoute.TYPES[OceanicRoute.PACOT], _or.getTypeName());
        _or.setType(OceanicRoute.TYPES[OceanicRoute.NAT]);
        assertEquals(OceanicRoute.TYPES[OceanicRoute.NAT], _or.getTypeName());
    }
    
    public void testValidation() {
        validateInput("type", new Integer(-1), IllegalArgumentException.class);
        validateInput("type", new Integer(11), IllegalArgumentException.class);
        validateInput("type", null, IllegalArgumentException.class);
        validateInput("type", "XXXXX", IllegalArgumentException.class);
        validateInput("source", null, NullPointerException.class);
    }
    
    public void testComparator() {
        Date d = new Date();
        Date d2 = new Date(d.getTime() + 100);
        _or.setDate(d);
        OceanicRoute or2 = new OceanicRoute(OceanicRoute.NAT);
        or2.setDate(d2);
        OceanicRoute or3 = new OceanicRoute(OceanicRoute.PACOT);
        or3.setDate(d);
        assertTrue(_or.compareTo(or2) < 0);
        assertTrue(_or.compareTo(or3) < 0);
        assertTrue(or2.compareTo(or3) > 0);
    }
}