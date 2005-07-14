package org.deltava.beans.event;

import java.io.*;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airport;
import org.hansel.CoverageDecorator;

public class TestFlightPlan extends AbstractBeanTestCase {

    private FlightPlan _fp;
    
    public static Test suite() {
        return new CoverageDecorator(TestFlightPlan.class, new Class[] { FlightPlan.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _fp = new FlightPlan(1);
        setBean(_fp);
    }

    protected void tearDown() throws Exception {
        _fp = null;
        super.tearDown();
    }
    
    public void testProperties() {
        checkProperty("airportD", new Airport("ATL", "KATL", "Atlanta GA"));
        checkProperty("airportA", new Airport("CLT", "KCLT", "Charlotte NC"));
        checkProperty("type", new Integer(FlightPlan.FSNAV));
        _fp.setType("Squawkbox 3.x");
        assertEquals(FlightPlan.SB3, _fp.getType());
        assertEquals("Squawkbox 3.x", _fp.getTypeName());
        assertEquals(-1, _fp.getSize());
        assertEquals("ATL-CLT.sfp", _fp.getFileName());
    }
    
    public void testValidation() {
        validateInput("type", new Integer(-1), IllegalArgumentException.class);
        validateInput("type", new Integer(FlightPlan.PLAN_TYPE.length), IllegalArgumentException.class);
        validateInput("type", "X", IllegalArgumentException.class);
    }
    
    public void testFileBuffer() throws IOException {
        // See what happens if we get the stream when the buffer is empty
        InputStream is = _fp.getInputStream();
        assertNotNull(is);
        assertEquals(0, is.available());
        
        File f = new File("data/stl-lga.fsn");
        assertTrue(f.exists());
        is = new FileInputStream(f);
        _fp.load(is);
        is.close();

        assertEquals(f.length(), _fp.getSize());
        InputStream fS = _fp.getInputStream();
        assertNotNull(fS);
        assertTrue(fS.available() == _fp.getSize());
    }
}