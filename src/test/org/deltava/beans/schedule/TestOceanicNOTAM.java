package org.deltava.beans.schedule;

import java.time.Instant;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.navdata.OceanicTrackInfo;

public class TestOceanicNOTAM extends AbstractBeanTestCase {

    private OceanicNOTAM _or;
    
    public static Test suite() {
        return new CoverageDecorator(TestOceanicNOTAM.class, new Class[] { OceanicNOTAM.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _or = new OceanicNOTAM(OceanicTrackInfo.Type.NAT, Instant.now());
        setBean(_or);
    }

    @Override
	protected void tearDown() throws Exception {
        _or = null;
        super.tearDown();
    }
    
    public void testProperties() {
        assertEquals(OceanicTrackInfo.Type.NAT, _or.getType());
        checkProperty("date", Instant.now());
        checkProperty("source", "localhost");
        checkProperty("route", "!@# SADJSAKD");
        checkProperty("type", OceanicTrackInfo.Type.PACOT);
    }
    
    public void testValidation() {
        validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("type", Integer.valueOf(11), IllegalArgumentException.class);
        validateInput("type", null, IllegalArgumentException.class);
        validateInput("type", "XXXXX", IllegalArgumentException.class);
        validateInput("source", null, NullPointerException.class);
    }
    
    public void testComparator() {
        Instant d = Instant.now();
        Instant d2 = d.plusMillis(100);
        _or.setDate(d);
        OceanicNOTAM or2 = new OceanicNOTAM(OceanicTrackInfo.Type.NAT, d2);
        OceanicNOTAM or3 = new OceanicNOTAM(OceanicTrackInfo.Type.PACOT, d);
        assertTrue(_or.compareTo(or2) < 0);
        assertTrue(_or.compareTo(or3) < 0);
        assertTrue(or2.compareTo(or3) > 0);
    }
}