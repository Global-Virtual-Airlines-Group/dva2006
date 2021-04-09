package org.deltava.beans.flight;

import java.time.*;
import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

public class TestFlightReport extends AbstractBeanTestCase {
    
    private FlightReport _fr;
    
    private static Airline _a = new Airline("DVA", "Delta Virtual Airlines");
    private static Airline _afv = new Airline("AFV", "Aviation Francais Virtuel");
    
    public static Test suite() {
        return new CoverageDecorator(TestFlightReport.class, new Class[] { Flight.class, FlightReport.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _fr = new FlightReport(_a, 123, 1);
        setBean(_fr);
    }
    
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        _fr = null;
    }
    
    public void testProperties() {
        assertEquals("DVA", _fr.getAirline().getCode());
        assertEquals(123, _fr.getFlightNumber());
        assertEquals(1, _fr.getLeg());
        _fr.setAirline(new Airline("dvc", "Delta Virtual Connection"));
        assertEquals("DVC", _fr.getAirline().getCode());
        checkProperty("ID", Integer.valueOf(123));
        checkProperty("rank", Rank.C);
        checkProperty("remarks", "REMARKS");
        checkProperty("attributes", Integer.valueOf(1234));
        checkProperty("submittedOn", Instant.ofEpochMilli(123457));
        checkProperty("disposedOn", Instant.ofEpochMilli(1234568));
        checkProperty("date", Instant.ofEpochMilli(12345));
        checkProperty("equipmentType", "CRJ-200");
        checkProperty("flightNumber", Integer.valueOf(_fr.getFlightNumber()));
        checkProperty("leg", Integer.valueOf(_fr.getLeg()));
        checkProperty("length", Integer.valueOf(21));
        checkProperty("status", FlightStatus.HOLD);
        checkProperty("simulator", Simulator.FS2002);
    }
    
    public void testValidation() {
        validateInput("flightNumber", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("leg", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("leg", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("leg", Integer.valueOf(6), IllegalArgumentException.class);
        validateInput("length", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("length", Integer.valueOf(181), IllegalArgumentException.class);
        validateInput("FSVersion", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("FSVersion", "X", IllegalArgumentException.class);
        validateInput("status", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("status", Integer.valueOf(21), IllegalArgumentException.class);
        validateInput("status", "X", IllegalArgumentException.class);
    }
    
    public void testAttributes() {
        assertEquals(0, _fr.getAttributes());
        _fr.setAttribute(FlightReport.ATTR_ACARS, true);
        assertTrue(_fr.hasAttribute(FlightReport.ATTR_ACARS));
        _fr.setAttribute(FlightReport.ATTR_IVAO, true);
        assertTrue(_fr.hasAttribute(FlightReport.ATTR_IVAO));
        _fr.setAttribute(FlightReport.ATTR_ACARS, false);
        assertFalse(_fr.hasAttribute(FlightReport.ATTR_ACARS));
    }
    
    public void testAirports() {
    	assertFalse(_fr.isPopulated());
    	assertEquals(-1, _fr.getDistance());
        
        Airport jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
        jfk.setLocation(40.6397, -73.7789);
        checkProperty("airportA", jfk);

        assertFalse(_fr.isPopulated());
    	assertEquals(-1, _fr.getDistance());
        
        Airport atl = new Airport("ATL", "KATL", "Atlanta GA");
        atl.setLocation(33.6404, -84.4269);
        checkProperty("airportD", atl);

        assertTrue(_fr.isPopulated());
        assertEquals(_fr.getDistance(), atl.getPosition().distanceTo(jfk.getPosition()));
    }
    
    public void testToString() {
        assertEquals(_fr.toString(), "DVA123 Leg 1");
    }
    
    public void testDatabaseIDs() {
        assertEquals(0, _fr.getDatabaseID(DatabaseID.EVENT));
        _fr.setDatabaseID(DatabaseID.EVENT, 123);
        assertEquals(123, _fr.getDatabaseID(DatabaseID.EVENT));

        try {
            _fr.setDatabaseID(DatabaseID.EVENT, -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
    }
    
    public void testComparison() {
        FlightReport fr2 = new FlightReport(_a, 123, 2);
        _fr.setDate(Instant.now());
        fr2.setDate(_fr.getDate().plusSeconds(1));
        assertEquals(-1, _fr.compareTo(fr2));
        assertEquals(1, fr2.compareTo(_fr));
        assertFalse(_fr.equals(fr2));
        assertFalse(_fr.equals(new Object()));
        
        Flight f2 = new FlightReport(_a, 123, 1);
        Flight f3 = new FlightReport(_afv, 123, 1);
        Flight f4 = new FlightReport(_a, 223, 1);
        
        assertEquals(0, _fr.compareTo(f2));
        assertTrue(_fr.compareTo(f3) > 0);
        assertTrue(_fr.equals(f2));
        assertFalse(_fr.equals(f3));
        assertFalse(_fr.equals(f4));
        assertTrue(_fr.compareTo(f4) < 0);
    }
    
    public void testFromFlight() {
       _fr.setEquipmentType("CRJ-200");
       _fr.setAirportD(new Airport("JFK", "KJFK", "New York-Kennedy NY"));
       _fr.setAirportA(new Airport("ATL", "KATL", "Atlanta GA"));
       
       FlightReport fr2 = new FlightReport(_fr);
       assertEquals(_fr.getAirline(), fr2.getAirline());
       assertEquals(_fr.getFlightNumber(), fr2.getFlightNumber());
       assertEquals(_fr.getLeg(), fr2.getLeg());
       assertEquals(_fr.getEquipmentType(), fr2.getEquipmentType());
       assertEquals(_fr.getAirportD(), fr2.getAirportD());
       assertEquals(_fr.getAirportA(), fr2.getAirportA());
    }
    
    public void testPromoEQ() {
       assertNotNull(_fr.getCaptEQType());
       
       Set<String> eqTypes = new HashSet<String>();
       eqTypes.add("B757-200");
       eqTypes.add("B767-300");
       _fr.setCaptEQType(eqTypes);
       
       assertEquals(2, _fr.getCaptEQType().size());
       assertTrue(_fr.getCaptEQType().contains("B757-200"));
       assertTrue(_fr.getCaptEQType().contains("B767-300"));
    }
    
    public void testDateAdjust() {
    	
    	TZInfo.init("US/Eastern", null, null);
    	TZInfo est = TZInfo.get("US/Eastern");
    	assertNotNull(est);

    	LocalDate sd = LocalDate.now().plusDays(1);
    	LocalDateTime ldt = sd.atTime(2, 0);
    	assertNotNull(ldt);
    	_fr.setDate(ldt.toInstant(ZoneOffset.UTC));
    	
    	LocalDate pd = ZonedDateTime.ofInstant(_fr.getDate(), est.getZone()).toLocalDate();
    	assertNotNull(pd);
    	Duration timeDelta = Duration.between(pd.atStartOfDay(), sd.atStartOfDay());
    	assertNotNull(timeDelta);
    	assertFalse(timeDelta.isNegative());
    	assertTrue(sd.getDayOfYear() != pd.getDayOfYear());
    	
    	Instant dt2 = _fr.getDate().minusSeconds(timeDelta.getSeconds());
    	assertNotNull(dt2);
    	LocalDate fd = ZonedDateTime.ofInstant(dt2, est.getZone()).toLocalDate();
    	assertEquals(pd.getDayOfYear(), fd.getDayOfYear());
    }
}