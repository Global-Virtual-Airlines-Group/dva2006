package org.deltava.beans;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

public class TestPilot extends AbstractBeanTestCase {
	
	public static Test suite() {
		return new CoverageDecorator(TestPilot.class, new Class[] { Pilot.class } );
   }

    private Pilot _p;
    
    protected void setUp() throws Exception {
        super.setUp();
        _p = new Pilot("John", "Smith");
        setBean(_p);
    }
    
    protected void tearDown() throws Exception {
        _p = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("John", _p.getFirstName());
        assertEquals("Smith", _p.getLastName());
        assertEquals("John Smith", _p.getName());
        assertEquals("", _p.getPilotCode());
        _p.setPilotCode(null);
        
        checkProperty("ID", new Integer(123));
        checkProperty("legs", new Integer(100));
        checkProperty("hours", new Double(323.3));
        checkProperty("miles", new Long(12345));
        checkProperty("onlineLegs", new Integer(101));
        checkProperty("onlineHours", new Double(323.4));
        checkProperty("lastFlight", new Date());
        checkProperty("hasSignature", Boolean.valueOf(true));
        checkProperty("showSignatures", Boolean.valueOf(true));
        checkProperty("showSSThreads", Boolean.valueOf(true));

        checkProperty("status", new Integer(3));
        assertEquals(Pilot.STATUS[3], _p.getStatusName());
        _p.setStatus(Pilot.STATUS[2]);
        assertEquals(2, _p.getStatus());
        
        checkProperty("airportCodeType", new Integer(Airport.ICAO));
        assertEquals(Airport.CODETYPES[Airport.ICAO], _p.getAirportCodeTypeName());
    }
    
    public void testComboAlias() {
    	_p.setID(123);
        assertEquals(String.valueOf(_p.getID()), _p.getComboAlias());
        assertEquals(_p.getName(), _p.getComboName());
    }
    
    public void testCacheKey() {
    	_p.setID(123);
    	assertEquals(new Integer(123), _p.cacheKey());
    }
    
    public void testRatings() {
        assertNotNull(_p.getRatings());
        assertEquals(0, _p.getRatings().size());
        assertFalse(_p.getRatings().contains("B737-800"));
        assertFalse(_p.hasRating("B737-800"));
        _p.addRating("B737-800");
        assertEquals(1, _p.getRatings().size());
        assertTrue(_p.getRatings().contains("B737-800"));
        assertTrue(_p.hasRating("B737-800"));
        _p.addRating("B737-200");
        assertEquals(2, _p.getRatings().size());
        assertTrue(_p.getRatings().contains("B737-200"));
        _p.addRating("B737-800");
        assertEquals(2, _p.getRatings().size());
        try {
            _p.addRating(null);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
        
        // Test removal
        ArrayList<String> allRatings = new ArrayList<String>(_p.getRatings());
        _p.removeRatings(allRatings);
        assertNotNull(_p.getRatings());
        assertEquals(0, _p.getRatings().size());
        
        // Test Addition
        _p.addRatings(allRatings);
        assertEquals(2, _p.getRatings().size());
    }
    
    public void testRoles() {
        assertNotNull(_p.getRoles());
        assertEquals(1, _p.getRoles().size());
        assertTrue(_p.getRoles().contains("Pilot"));
        assertTrue(_p.isInRole("Pilot"));
        assertTrue(_p.isInRole("*"));
        
        assertFalse(_p.getRoles().contains("HR"));
        assertFalse(_p.isInRole("HR"));
        
        _p.addRole("HR");
        assertEquals(2, _p.getRoles().size());
        assertTrue(_p.getRoles().contains("HR"));
        _p.addRole("Developer");
        assertEquals(3, _p.getRoles().size());
        assertTrue(_p.getRoles().contains("Developer"));
        _p.addRole("Developer");
        assertEquals(3, _p.getRoles().size());
        assertTrue(_p.getRoles().contains("Developer"));
        try {
            _p.addRole(null);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
        
        _p.addRole("Admin");
        assertTrue(_p.getRoles().contains("Admin"));
        assertTrue(_p.isInRole("ANYROLE"));
        
        Set<String> rNames = new HashSet<String>();
        rNames.add("Role2");
        rNames.add("Role3");
        rNames.add("Pilot");
        _p.addRoles(rNames);
        
        assertTrue(_p.isInRole("Role2"));
        assertTrue(_p.isInRole("Role3"));

        rNames.add("Admin");
        _p.removeRoles(rNames);
        assertFalse(_p.isInRole("Role2"));
        assertFalse(_p.isInRole("Role3"));
        assertTrue(_p.isInRole("Pilot"));
        assertTrue(_p.isInRole("*"));
    }
    
    public void testPilotCode() {
        _p.setPilotCode("DVA043");
        assertEquals("DVA043", _p.getPilotCode());
        assertEquals(43, _p.getPilotNumber());
        assertEquals("DVA", _p.getAirlineCode());
        
        _p.setPilotCode("DVA-043");
        assertEquals("DVA043", _p.getPilotCode());
        assertEquals(43, _p.getPilotNumber());
        assertEquals("DVA", _p.getAirlineCode());
    }
    
    public void testValidation() {
    	validateInput("ID", new Integer(0), IllegalArgumentException.class);
    	validateInput("status", new Integer(81), IllegalArgumentException.class);
    	validateInput("status", "XXX", IllegalArgumentException.class);
    	validateInput("legs", new Integer(-1), IllegalArgumentException.class);
    	validateInput("hours", new Double(-1.1), IllegalArgumentException.class);
    	validateInput("onlineLegs", new Integer(-1), IllegalArgumentException.class);
    	validateInput("onlineHours", new Double(-1.1), IllegalArgumentException.class);
    	validateInput("miles", new Long(-1), IllegalArgumentException.class);
    	
    	try {
    		_p.setPilotCode("DVAXXX");
    		fail("IllegalArgumentException expected");
    	} catch (IllegalArgumentException iae) { 
    		// empty
    	}
    }
    
    public void testFlights() {
    	FlightReport fr = new FlightReport(new Airline("DVA"), 123, 1);
    	fr.setDate(new Date());
    	fr.setLength(11);
    	
        Airport jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
        jfk.setLocation(40.6397, -73.7789);
        fr.setAirportD(jfk);
        
        Airport atl = new Airport("ATL", "KATL", "Atlanta GA");
        atl.setLocation(33.6404, -84.4269);
        fr.setAirportA(atl);
    	
    	assertEquals(1, _p.getLegs());
    	assertEquals(fr.getDate(), _p.getLastFlight());
    	assertEquals(0.0, _p.getHours() , 0.001);
    	assertEquals(0, _p.getOnlineLegs());
    	assertEquals(0.0, _p.getOnlineHours() , 0.001);
    	
    	fr.setStatus(FlightReport.OK);
    	assertEquals(1.1, _p.getHours() , 0.001);
    	
    	assertEquals(0.0, _p.getOnlineHours(), 0.001);
    	assertEquals(0, _p.getOnlineLegs());
    	fr.setAttribute(FlightReport.ATTR_IVAO, true);
    	assertEquals(1.1, _p.getOnlineHours(), 0.001);
    	assertEquals(1, _p.getOnlineLegs());
    	assertEquals(fr.getDistance(), _p.getMiles());
    }
    
    public void testViewEntry() {
       String[] ROW_CLASSES = {null, "opt2", "opt3", "opt1", "err", "warn"};
       for (int x = 0; x < ROW_CLASSES.length; x++) {
          _p.setStatus(x);
          assertEquals(ROW_CLASSES[x], _p.getRowClassName());
       }
    }
}