package org.deltava.beans;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

public class TestFlightReport extends AbstractBeanTestCase {
    
    private FlightReport _fr;
    
    private static Airline _a = new Airline("DVA", "Delta Virtual Airlines");
    private static Airline _afv = new Airline("AFV", "Aviation Francais Virtuel");
    
    public static Test suite() {
        return new CoverageDecorator(TestFlightReport.class, new Class[] { Flight.class, FlightReport.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _fr = new FlightReport(_a, 123, 1);
        setBean(_fr);
    }
    
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
        checkProperty("ID", new Integer(123));
        checkProperty("firstName", "John");
        checkProperty("lastName", "Smith");
        checkProperty("rank", "Captain");
        checkProperty("remarks", "REMARKS");
        checkProperty("attributes", new Integer(1234));
        checkProperty("createdOn", new Date(123456));
        checkProperty("submittedOn", new Date(123457));
        checkProperty("disposedOn", new Date(1234568));
        checkProperty("date", new Date(12345));
        checkProperty("equipmentType", "CRJ-200");
        checkProperty("flightNumber", new Integer(_fr.getFlightNumber()));
        checkProperty("leg", new Integer(_fr.getLeg()));
        checkProperty("length", new Integer(21));
        checkProperty("status", new Integer(2));
        assertEquals(FlightReport.STATUS[_fr.getStatus()], _fr.getStatusName());
        checkProperty("FSVersion", new Integer(2002));
        _fr.setFSVersion("FS2000");
        assertEquals(2000, _fr.getFSVersion());
        _fr.setStatus("Hold");
        assertEquals(FlightReport.HOLD, _fr.getStatus());
    }
    
    public void testValidation() {
        validateInput("flightNumber", new Integer(-1), IllegalArgumentException.class);
        validateInput("leg", new Integer(-1), IllegalArgumentException.class);
        validateInput("leg", new Integer(0), IllegalArgumentException.class);
        validateInput("leg", new Integer(6), IllegalArgumentException.class);
        validateInput("length", new Integer(-1), IllegalArgumentException.class);
        validateInput("length", new Integer(181), IllegalArgumentException.class);
        validateInput("FSVersion", new Integer(-1), IllegalArgumentException.class);
        validateInput("FSVersion", "X", IllegalArgumentException.class);
        validateInput("status", new Integer(-1), IllegalArgumentException.class);
        validateInput("status", new Integer(21), IllegalArgumentException.class);
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
        try {
            _fr.getDistance();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
        	// empty
        }
        
        Airport jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
        jfk.setLocation(40.6397, -73.7789);
        checkProperty("airportA", jfk);

        try {
            _fr.getDistance();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
        	// empty
        }
        
        Airport atl = new Airport("ATL", "KATL", "Atlanta GA");
        atl.setLocation(33.6404, -84.4269);
        checkProperty("airportD", atl);
        
        assertEquals(_fr.getDistance(), atl.getPosition().distanceTo(jfk.getPosition()));
    }
    
    public void testToString() {
        assertEquals(_fr.toString(), "DVA123 Leg 1");
    }
    
    public void testDatabaseIDs() {
        assertEquals(0, _fr.getDatabaseID("EVENT"));
        _fr.setDatabaseID("EVENT", 123);
        assertEquals(123, _fr.getDatabaseID("EVENT"));
        try {
            _fr.setDatabaseID(null, 123);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }

        try {
            _fr.setDatabaseID("EVENT", -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
    }
    
    public void testComparison() {
        FlightReport fr2 = new FlightReport(_a, 123, 2);
        _fr.setDate(new Date());
        fr2.setDate(new Date(_fr.getDate().getTime() + 1));
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
    
    public void testViewEntry() {
       String[] ROW_CLASSES = {"opt2", "opt1", "warn", null, "err"};
       for (int x = 0; x < ROW_CLASSES.length; x++) {
          _fr.setStatus(x);
          assertEquals(ROW_CLASSES[x], _fr.getRowClassName());
       }
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
}