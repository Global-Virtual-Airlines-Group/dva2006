package org.deltava.beans.schedule;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.navdata.OceanicTrackInfo;

public class TestOceanicNOTAM extends AbstractBeanTestCase {

    private OceanicNOTAM _or;
    
    public static Test suite() {
        return new CoverageDecorator(TestOceanicNOTAM.class, new Class[] { OceanicNOTAM.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _or = new OceanicNOTAM(OceanicTrackInfo.Type.NAT, new Date());
        setBean(_or);
    }

    protected void tearDown() throws Exception {
        _or = null;
        super.tearDown();
    }
    
    public void testProperties() {
        assertEquals(OceanicTrackInfo.Type.NAT, _or.getType());
        assertEquals(OceanicTrackInfo.TYPES[OceanicTrackInfo.Type.NAT.ordinal()], _or.getTypeName());
        checkProperty("date", new Date());
        checkProperty("source", "localhost");
        checkProperty("route", "!@# SADJSAKD");
        checkProperty("type", OceanicTrackInfo.Type.PACOT);
        assertEquals(OceanicTrackInfo.TYPES[OceanicTrackInfo.Type.PACOT.ordinal()], _or.getTypeName());
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
        OceanicNOTAM or2 = new OceanicNOTAM(OceanicTrackInfo.Type.NAT, d2);
        OceanicNOTAM or3 = new OceanicNOTAM(OceanicTrackInfo.Type.PACOT, d);
        assertTrue(_or.compareTo(or2) < 0);
        assertTrue(_or.compareTo(or3) < 0);
        assertTrue(or2.compareTo(or3) > 0);
    }
}