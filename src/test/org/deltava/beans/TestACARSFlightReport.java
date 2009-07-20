// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Date;

import org.junit.*;
import org.hansel.*;
import org.junit.runner.*;

import org.deltava.beans.schedule.Airline;

@RunWith(CoverageRunner.class)
@org.junit.runners.Suite.SuiteClasses({ TestACARSFlightReport.class })
@CoverageRunner.CoverClasses({ ACARSFlightReport.class })
public class TestACARSFlightReport extends AbstractBeanTestCase {

    private ACARSFlightReport _fr;
    
    public static junit.framework.Test suite() {
    	return new junit.framework.JUnit4TestAdapter(TestACARSFlightReport.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _fr = new ACARSFlightReport(new Airline("DVA", "Delta Virtual Airlines"), 43, 1);
        setBean(_fr);
    }

    protected void tearDown() throws Exception {
        _fr = null;
        super.tearDown();
    }

    @Test public void testProperties() {
        assertEquals("DVA", _fr.getAirline().getCode());
        assertEquals(43, _fr.getFlightNumber());
        assertEquals(1, _fr.getLeg());
        
        checkProperty("route", "DIRECT");
        checkProperty("endTime", new Date());
        checkProperty("engineStartTime", new Date());
        checkProperty("landingTime", new Date());
        checkProperty("startTime", new Date());
        checkProperty("takeoffTime", new Date());
        checkProperty("taxiTime", new Date());
        checkProperty("gateFuel", new Integer(15000));
        checkProperty("landingFuel", new Integer(15000));
        checkProperty("takeoffFuel", new Integer(15000));
        checkProperty("taxiFuel", new Integer(15000));
        checkProperty("gateWeight", new Integer(15000));
        checkProperty("landingWeight", new Integer(15000));
        checkProperty("taxiWeight", new Integer(15000));
        checkProperty("takeoffWeight", new Integer(15000));
        checkProperty("landingDistance", new Integer(23));
        checkProperty("takeoffDistance", new Integer(23));
        checkProperty("takeoffSpeed", new Integer(123));
        checkProperty("landingSpeed", new Integer(113));
        checkProperty("landingVSpeed", new Integer(-123));
        checkProperty("landingN1", new Double(23.2));
        checkProperty("takeoffN1", new Double(93.1));
        checkProperty("time1X", new Integer(421));
        checkProperty("time2X", new Integer(422));
        checkProperty("time4X", new Integer(424));
    }
    
    @Test public void testTimeCalculation() {
        long ts = System.currentTimeMillis() - 86400000;
        _fr.setStartTime(new Date(ts));
        _fr.setTakeoffTime(new Date(ts + 35000));
        _fr.setLandingTime(new Date(ts + 3600000));
        _fr.setEndTime(new Date(ts + 3610000));
        
        assertEquals(3610000, _fr.getBlockTime().getTime());
        assertEquals(3565000, _fr.getAirborneTime().getTime());
    }
    
    @Test public void testValidation() {
        validateInput("gateWeight", new Integer(0), IllegalArgumentException.class);
        validateInput("gateFuel", new Integer(-1), IllegalArgumentException.class);
        validateInput("landingWeight", new Integer(-1), IllegalArgumentException.class);
        validateInput("landingSpeed", new Integer(-1), IllegalArgumentException.class);
        validateInput("landingVSpeed", new Integer(1), IllegalArgumentException.class);
        validateInput("landingFuel", new Integer(-1), IllegalArgumentException.class);
        validateInput("takeoffWeight", new Integer(0), IllegalArgumentException.class);
        validateInput("takeoffSpeed", new Integer(-1), IllegalArgumentException.class);
        validateInput("takeoffFuel", new Integer(-1), IllegalArgumentException.class);
        validateInput("taxiWeight", new Integer(0), IllegalArgumentException.class);
        validateInput("taxiFuel", new Integer(-1), IllegalArgumentException.class);
        validateInput("landingN1", new Double(-1), IllegalArgumentException.class);
        validateInput("landingN1", new Double(141), IllegalArgumentException.class);
        validateInput("takeoffN1", new Double(-1), IllegalArgumentException.class);
        validateInput("takeoffN1", new Double(141), IllegalArgumentException.class);
        validateInput("takeoffDistance", new Integer(-1), IllegalArgumentException.class);
        validateInput("landingDistance", new Integer(-1), IllegalArgumentException.class);
        validateInput("time1X", new Integer(-1), IllegalArgumentException.class);
        validateInput("time2X", new Integer(-1), IllegalArgumentException.class);
        validateInput("time4X", new Integer(-1), IllegalArgumentException.class);
        
        try {
            assertNull(_fr.getBlockTime());
        } catch (IllegalStateException ise) {
        	// empty
        }
        
        try {
            assertNull(_fr.getAirborneTime());
        } catch (IllegalStateException ise) {
        	// empty
        }
    }
}