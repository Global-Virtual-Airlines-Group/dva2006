package org.deltava.beans.event;

import java.time.Instant;
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
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _e = new Event("NAME");
        setBean(_e);
    }

    @Override
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
        checkProperty("network", Integer.valueOf(1));
        assertEquals(Status.COMPLETE, _e.getStatus());
        _e.setNetwork(OnlineNetwork.VATSIM);
        assertEquals(_e.getNetwork(), OnlineNetwork.VATSIM);
        _e.setNetwork(OnlineNetwork.valueOf("IVAO"));
        assertEquals(_e.getNetwork(), OnlineNetwork.IVAO);
        _e.setStatus(Status.CANCELED);
        assertEquals(Status.CANCELED, _e.getStatus());
        _e.setStatus(Status.OPEN);
    }
  
    public void testComboAlias() {
        _e.setID(123);
        assertEquals(_e.getName(), _e.getComboName());
        assertEquals(String.valueOf(_e.getID()), _e.getComboAlias());
    }
    
    public void testEmptyLists() {
        assertNotNull(_e.getCharts());
        assertNotNull(_e.getSignups());
        assertNotNull(_e.getAssignments());
        assertEquals(0, _e.getCharts().size());
        assertEquals(0, _e.getSignups().size());
        assertEquals(0, _e.getAssignments().size());
    }
    
    public void testStatus() {
        _e.setStartTime(Instant.now().plusSeconds(1));
        _e.setSignupDeadline(_e.getStartTime().plusSeconds(1));
        assertEquals(Status.OPEN, _e.getStatus());
        _e.setSignupDeadline(_e.getStartTime().minusSeconds(5));
        assertEquals(Status.CLOSED, _e.getStatus());
        _e.setStartTime(Instant.now().minusMillis(10));
        _e.setEndTime(_e.getStartTime().plusSeconds(5));
        assertEquals(Status.ACTIVE, _e.getStatus());
        _e.setEndTime(Instant.now().minusMillis(10));
        assertEquals(Status.COMPLETE, _e.getStatus());
    }
    
    public void testValidation() {
        validateInput("network", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("network", Integer.valueOf(11), IllegalArgumentException.class);
        validateInput("network", "X", IllegalArgumentException.class);
        
        _e.setStartTime(Instant.now());
        validateInput("signupDeadline", _e.getStartTime().plusSeconds(1), IllegalArgumentException.class);
        validateInput("endTime", _e.getStartTime().minusSeconds(1), IllegalArgumentException.class);
    }
    
    public void testComparator() {
        _e.setStartTime(Instant.now());
        
        Event e2 = new Event("EVENT2");
        e2.setStartTime(_e.getStartTime().plusMillis(5));
        
        assertTrue(_e.compareTo(e2) < 0);
        assertTrue(e2.compareTo(_e) > 0);
    }
    
    public void testLists() {
        Chart c = new Chart("MACEY TWO ARRIVAL", new Airport("ATL", "KATL", "Atlanta GA"));
        AssignmentInfo info = new AssignmentInfo("CRJ-200");
        
        _e.addAssignment(info);
        _e.addChart(c);
        
        assertEquals(1, _e.getAssignments().size());
        assertEquals(1, _e.getCharts().size());
    }
}