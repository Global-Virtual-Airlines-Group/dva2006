package org.deltava.beans.assign;

import java.time.Instant;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Airline;

public class TestAssignmentInfo extends AbstractBeanTestCase {

    private AssignmentInfo _info;
    
    public static Test suite() {
        return new CoverageDecorator(TestAssignmentInfo.class, new Class[] { AssignmentInfo.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _info = new AssignmentInfo("B737-300");
        setBean(_info);
    }

    @Override
	protected void tearDown() throws Exception {
        _info = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("B737-300", _info.getEquipmentType());
        assertEquals(AssignmentStatus.AVAILABLE, _info.getStatus());
        checkProperty("pilotID", Integer.valueOf(0));
        checkProperty("pilotID", Integer.valueOf(8012));
        checkProperty("eventID", Integer.valueOf(0));
        checkProperty("eventID", Integer.valueOf(1234));
        checkProperty("status", AssignmentStatus.RESERVED);
        checkProperty("assignDate", Instant.now());
        checkProperty("completionDate", Instant.now());
        _info.setStatus(AssignmentStatus.COMPLETE);
        assertEquals(AssignmentStatus.COMPLETE, _info.getStatus());
        assertEquals("opt3", _info.getRowClassName());
    }
    
    public void testBooleanProperties() {
        assertFalse(_info.isPurgeable());
        assertFalse(_info.isRandom());
        assertFalse(_info.isRepeating());
        _info.setPurgeable(true);
        _info.setRandom(true);
        _info.setRepeating(true);
        assertTrue(_info.isPurgeable());
        assertTrue(_info.isRandom());
        assertTrue(_info.isRepeating());
    }
    
    public void testTimestamp() {
    	Instant now = Instant.now();
        _info.setAssignDate(now);
        assertEquals(now, _info.getAssignDate());
        _info.setCompletionDate(now);
        assertEquals(now, _info.getCompletionDate());
        _info.setAssignDate(null);
        assertNull(_info.getAssignDate());
        _info.setCompletionDate(null);
        assertNull(_info.getCompletionDate());
    }
    
    public void testValidation() {
        validateInput("eventID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("status", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("status", Integer.valueOf(21), IllegalArgumentException.class);
        validateInput("status", "X", IllegalArgumentException.class);
    }
    
    public void testLists() {
        assertNotNull(_info.getAssignments());
        assertNotNull(_info.getFlights());
        assertEquals(0, _info.getAssignments().size());
        assertEquals(0, _info.getFlights().size());
        
        AssignmentLeg a = new AssignmentLeg(new Airline("DVA", "Delta Virtual"), 129, 1);
        _info.addAssignment(a);
        assertEquals(1, _info.getAssignments().size());
        assertSame(a, _info.getAssignments().iterator().next());
        
        FlightReport fr = new FlightReport(new Airline("DVA", "Delta Virtual"), 123, 1);
        _info.addFlight(fr);
        assertEquals(1, _info.getFlights().size());
        assertSame(fr, _info.getFlights().iterator().next());
        assertEquals(0, fr.getDatabaseID(DatabaseID.PILOT));
        
        _info.setPilotID(1234);
        fr = new FlightReport(new Airline("DVA", "Delta Virtual"), 129, 1);
        _info.addFlight(fr);
        assertEquals(2, _info.getFlights().size());
        assertEquals(_info.getPilotID(), fr.getDatabaseID(DatabaseID.PILOT));
    }
}
