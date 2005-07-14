package org.deltava.beans.schedule;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.Flight;

public class TestScheduleEntry extends AbstractBeanTestCase {
    
    private Airline _dva = new Airline("DVA", "Delta Virtual Airlines");
    private Airline _afv = new Airline("AFV", "Aviation Francais Virtuel");
    
    private ScheduleEntry _e;
    private Airport _atl;
    private Airport _jfk;
    
    public static Test suite() {
        return new CoverageDecorator(TestScheduleEntry.class, new Class[] { ScheduleEntry.class, Flight.class });
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _atl = new Airport("ATL", "KATL", "Atlanta GA");
        _atl.setLocation(34.6404, -84.4269);
        _atl.setTZ("US/Eastern");
        _jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
        _jfk.setLocation(40.6397, -73.7789);
        _jfk.setTZ("US/Eastern");
        
        _e = new ScheduleEntry(_dva, 129, 1);
        setBean(_e);
    }

    protected void tearDown() throws Exception {
        _e = null;
        super.tearDown();
    }

    public void testFlightProperties() {
        assertEquals("DVA", _e.getAirline().getCode());
        assertEquals(129, _e.getFlightNumber());
        assertEquals(1, _e.getLeg());
        assertEquals("DVA129 Leg 1", _e.toString());
        checkProperty("equipmentType", "B767-300");
        checkProperty("airportA", new Airport("ATL", "KATL", "Atlanta GA"));
        checkProperty("airportD", new Airport("CLT", "KCLT", "Charlotte NC"));
        assertTrue(_e.equals(new ScheduleEntry(_dva, 129, 1)));
        assertFalse(_e.equals(new ScheduleEntry(_dva, 130, 1)));
        assertFalse(_e.equals(new ScheduleEntry(_dva, 129, 2)));
        assertFalse(_e.equals(new ScheduleEntry(_afv, 129, 1)));
    }
    
    public void testValidation() {
        validateInput("leg", new Integer(-1), IllegalArgumentException.class);
        validateInput("leg", new Integer(6), IllegalArgumentException.class);
        validateInput("flightNumber", new Integer(-1), IllegalArgumentException.class);
        
        validateInput("timeD", "12:35", NullPointerException.class);
        validateInput("timeA", "12:35", NullPointerException.class);
        
        _e.setAirportD(_atl);
        _e.setAirportA(_jfk);

        validateInput("timeD", "XX", IllegalArgumentException.class);
        validateInput("timeA", "XX", IllegalArgumentException.class);
    }
    
    public void testProperties() {
        assertFalse(_e.isHistoric());
        assertFalse(_e.canPurge());
        _e.setPurge(true);
        _e.setHistoric(true);
        assertTrue(_e.isHistoric());
        assertTrue(_e.canPurge());
        
        _e.setAirportD(_atl);
        _e.setAirportA(_jfk);
        assertEquals("ATL", _e.getAirportD().getIATA());
        assertEquals("JFK", _e.getAirportA().getIATA());
    }
    
    public void testDistance() {
        try {
            assertEquals(0, _e.getDistance());
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        _e.setAirportA(_jfk);
        
        try {
            assertEquals(0, _e.getDistance());
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        _e.setAirportD(_atl);
        assertEquals(715, _e.getDistance());
    }
    
    public void testLength() {
        _e.setAirportD(_atl);
        _e.setAirportA(_jfk);
        try {
            assertEquals(0, _e.getLength());
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        _e.setTimeA("12:05");
        assertNotNull(_e.getDateTimeA());
        try {
            assertEquals(0, _e.getLength());
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }

        _e.setTimeD("10:15");
        assertNotNull(_e.getDateTimeD());
        assertEquals(18, _e.getLength());
    }
    
    public void testComparator() {
        ScheduleEntry e3 = new ScheduleEntry(_afv, 129, 1);
        ScheduleEntry e4 = new ScheduleEntry(_dva, 129, 2);
        ScheduleEntry e5 = new ScheduleEntry(_dva, 130, 1);
        assertEquals(0, _e.compareTo(_e));
        assertTrue(_e.compareTo(e3) > 0);
        assertTrue(_e.compareTo(e4) < 0);
        assertTrue(_e.compareTo(e5) < 0);
    }
}

