package org.deltava.beans.event;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.Chart;

public class TestEvent extends AbstractBeanTestCase {

    private Event _e;
    
    public static Test suite() {
        return new CoverageDecorator(TestEvent.class, new Class[] { Event.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _e = new Event("NAME");
        setBean(_e);
    }

    protected void tearDown() throws Exception {
        _e = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("NAME", _e.getName());
        checkProperty("briefing", "REMARKS");
        checkProperty("route", "KATL SPA KCLT");
        long now = System.currentTimeMillis() - 100;
        checkProperty("startTime", new Date());
        checkProperty("signupDeadline", new Date(now));
        checkProperty("endTime", new Date());
        checkProperty("airportA", new Airport("ATL", "KATL", "Atlanta GA"));
        checkProperty("network", new Integer(1));
        assertEquals(Event.COMPLETE, _e.getStatus());
        _e.setNetwork(OnlineNetwork.VATSIM);
        assertEquals(_e.getNetwork(), OnlineNetwork.VATSIM);
        assertEquals("VATSIM", _e.getNetworkName());
        _e.setNetwork(OnlineNetwork.valueOf("IVAO"));
        assertEquals(_e.getNetwork(), OnlineNetwork.IVAO);
        assertEquals("IVAO", _e.getNetworkName());
        _e.setStatus(Event.CANCELED);
        assertEquals(Event.CANCELED, _e.getStatus());
        _e.setStatus(Event.OPEN);
    }
  
    public void testComboAlias() {
        _e.setID(123);
        assertEquals(_e.getName(), _e.getComboName());
        assertEquals(String.valueOf(_e.getID()), _e.getComboAlias());
    }
    
    public void testEmptyLists() {
        assertNotNull(_e.getCharts());
        assertNotNull(_e.getPlans());
        assertNotNull(_e.getSignups());
        assertNotNull(_e.getAssignments());
        assertEquals(0, _e.getCharts().size());
        assertEquals(0, _e.getPlans().size());
        assertEquals(0, _e.getSignups().size());
        assertEquals(0, _e.getAssignments().size());
    }
    
    public void testStatus() {
        long now = System.currentTimeMillis();
        _e.setStartTime(new Date(now + 1000));
        _e.setSignupDeadline(new Date(now + 1000));
        assertEquals(Event.OPEN, _e.getStatus());
        assertEquals(Event.STATUS[_e.getStatus()], _e.getStatusName());
        _e.setSignupDeadline(new Date(now - 5));
        assertEquals(Event.CLOSED, _e.getStatus());
        assertEquals(Event.STATUS[_e.getStatus()], _e.getStatusName());
        _e.setStartTime(new Date(now - 4));
        _e.setEndTime(new Date(now + 1000));
        assertEquals(Event.ACTIVE, _e.getStatus());
        assertEquals(Event.STATUS[_e.getStatus()], _e.getStatusName());
        _e.setEndTime(new Date(now - 3));
        assertEquals(Event.COMPLETE, _e.getStatus());
        assertEquals(Event.STATUS[_e.getStatus()], _e.getStatusName());
    }
    
    public void testValidation() {
        validateInput("network", new Integer(-1), IllegalArgumentException.class);
        validateInput("network", new Integer(11), IllegalArgumentException.class);
        validateInput("network", "X", IllegalArgumentException.class);
        
        long now = System.currentTimeMillis();
        _e.setStartTime(new Date(now));
        validateInput("signupDeadline", new Date(now + 1), IllegalArgumentException.class);
        validateInput("endTime", new Date(now - 1), IllegalArgumentException.class);
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        _e.setStartTime(new Date(now));
        
        Event e2 = new Event("EVENT2");
        e2.setStartTime(new Date(now + 2));
        
        assertTrue(_e.compareTo(e2) < 0);
        assertTrue(e2.compareTo(_e) > 0);
    }
    
    public void testLists() {
        Chart c = new Chart("MACEY TWO ARRIVAL", new Airport("ATL", "KATL", "Atlanta GA"));
        AssignmentInfo info = new AssignmentInfo("CRJ-200");
        FlightPlan fp = new FlightPlan(1);
        
        _e.addAssignment(info);
        _e.addChart(c);
        _e.addPlan(fp);
        
        assertEquals(1, _e.getAssignments().size());
        assertEquals(1, _e.getCharts().size());
        assertEquals(1, _e.getPlans().size());
    }
}