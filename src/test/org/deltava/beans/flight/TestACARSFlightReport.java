// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.Instant;

import org.junit.*;
import org.hansel.*;
import org.junit.runner.*;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airline;

@RunWith(CoverageRunner.class)
@org.junit.runners.Suite.SuiteClasses({ TestACARSFlightReport.class })
@CoverageRunner.CoverClasses({ ACARSFlightReport.class })
public class TestACARSFlightReport extends AbstractBeanTestCase {

    private ACARSFlightReport _fr;
    
    public static junit.framework.Test suite() {
    	return new junit.framework.JUnit4TestAdapter(TestACARSFlightReport.class);
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _fr = new ACARSFlightReport(new Airline("DVA", "Delta Virtual Airlines"), 43, 1);
        setBean(_fr);
    }

    @Override
	protected void tearDown() throws Exception {
        _fr = null;
        super.tearDown();
    }

    @Test public void testProperties() {
        assertEquals("DVA", _fr.getAirline().getCode());
        assertEquals(43, _fr.getFlightNumber());
        assertEquals(1, _fr.getLeg());
        
        checkProperty("route", "DIRECT");
        checkProperty("endTime", Instant.now());
        checkProperty("engineStartTime", Instant.now());
        checkProperty("landingTime", Instant.now());
        checkProperty("startTime", Instant.now());
        checkProperty("takeoffTime", Instant.now());
        checkProperty("taxiTime", Instant.now());
        checkProperty("gateFuel", Integer.valueOf(15000));
        checkProperty("landingFuel", Integer.valueOf(15000));
        checkProperty("takeoffFuel", Integer.valueOf(15000));
        checkProperty("taxiFuel", Integer.valueOf(15000));
        checkProperty("gateWeight", Integer.valueOf(15000));
        checkProperty("landingWeight", Integer.valueOf(15000));
        checkProperty("taxiWeight", Integer.valueOf(15000));
        checkProperty("takeoffWeight", Integer.valueOf(15000));
        checkProperty("landingDistance", Integer.valueOf(23));
        checkProperty("takeoffDistance", Integer.valueOf(23));
        checkProperty("takeoffSpeed", Integer.valueOf(123));
        checkProperty("landingSpeed", Integer.valueOf(113));
        checkProperty("landingVSpeed", Integer.valueOf(-123));
        checkProperty("landingN1", new Double(23.2));
        checkProperty("takeoffN1", new Double(93.1));
        checkProperty("time1X", Integer.valueOf(421));
        checkProperty("time2X", Integer.valueOf(422));
        checkProperty("time4X", Integer.valueOf(424));
    }
    
    @Test public void testTimeCalculation() {
        _fr.setStartTime(Instant.now());
        _fr.setTakeoffTime(_fr.getStartTime().plusSeconds(35));
        _fr.setLandingTime(_fr.getStartTime().plusSeconds(3600));
        _fr.setEndTime(_fr.getStartTime().plusSeconds(3610));
        
        assertEquals(3610, _fr.getBlockTime().getSeconds());
        assertEquals(3565, _fr.getAirborneTime().getSeconds());
    }
    
    @Test public void testValidation() {
        validateInput("gateWeight", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("gateFuel", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("landingWeight", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("landingSpeed", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("landingVSpeed", Integer.valueOf(1), IllegalArgumentException.class);
        validateInput("landingFuel", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("takeoffWeight", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("takeoffSpeed", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("takeoffFuel", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("taxiWeight", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("taxiFuel", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("landingN1", new Double(-1), IllegalArgumentException.class);
        validateInput("landingN1", new Double(141), IllegalArgumentException.class);
        validateInput("takeoffN1", new Double(-1), IllegalArgumentException.class);
        validateInput("takeoffN1", new Double(141), IllegalArgumentException.class);
        validateInput("takeoffDistance", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("landingDistance", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("time1X", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("time2X", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("time4X", Integer.valueOf(-1), IllegalArgumentException.class);
        
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