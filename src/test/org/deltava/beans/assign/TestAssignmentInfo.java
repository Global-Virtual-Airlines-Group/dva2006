package org.deltava.beans.assign;

import java.util.Date;
import java.sql.Timestamp;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Airline;

public class TestAssignmentInfo extends AbstractBeanTestCase {

    private AssignmentInfo _info;
    
    public static Test suite() {
        return new CoverageDecorator(TestAssignmentInfo.class, new Class[] { AssignmentInfo.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _info = new AssignmentInfo("B737-300");
        setBean(_info);
    }

    protected void tearDown() throws Exception {
        _info = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("B737-300", _info.getEquipmentType());
        assertEquals(AssignmentInfo.AVAILABLE, _info.getStatus());
        checkProperty("pilotID", new Integer(0));
        checkProperty("pilotID", new Integer(8012));
        checkProperty("eventID", new Integer(0));
        checkProperty("eventID", new Integer(1234));
        checkProperty("status", new Integer(AssignmentInfo.RESERVED));
        checkProperty("assignDate", new Date());
        checkProperty("completionDate", new Date());
        _info.setStatus("Complete");
        assertEquals(AssignmentInfo.COMPLETE, _info.getStatus());
        assertEquals("opt3", _info.getRowClassName());
        
        Pilot p = new Pilot("John", "Smith");
        p.setID(12);
        _info.setPilotID(p);
        assertEquals(p.getID(), _info.getPilotID());
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
        long now = System.currentTimeMillis();
        _info.setAssignDate(new Timestamp(now));
        assertEquals(now, _info.getAssignDate().getTime());
        _info.setCompletionDate(new Timestamp(now));
        assertEquals(now, _info.getCompletionDate().getTime());
        _info.setAssignDate((Timestamp) null);
        assertNull(_info.getAssignDate());
        _info.setCompletionDate((Timestamp) null);
        assertNull(_info.getCompletionDate());
    }
    
    public void testValidation() {
        validateInput("eventID", new Integer(-1), IllegalArgumentException.class);
        validateInput("status", new Integer(-1), IllegalArgumentException.class);
        validateInput("status", new Integer(21), IllegalArgumentException.class);
        validateInput("status", "X", IllegalArgumentException.class);
    }
    
    public void testLists() {
        assertNotNull(_info.getAssignments());
        assertNotNull(_info.getFlights());
        assertEquals(0, _info.getAssignments().size());
        assertEquals(0, _info.getFlights().size());
        
        AssignmentLeg a = new AssignmentLeg(new Airline("DVA"), 129, 1);
        _info.addAssignment(a);
        assertEquals(1, _info.getAssignments().size());
        assertSame(a, _info.getAssignments().iterator().next());
        
        FlightReport fr = new FlightReport(new Airline("DVA"), 123, 1);
        _info.addFlight(fr);
        assertEquals(1, _info.getFlights().size());
        assertSame(fr, _info.getFlights().iterator().next());
        assertEquals(0, fr.getDatabaseID(FlightReport.DBID_PILOT));
        
        _info.setPilotID(1234);
        fr = new FlightReport(new Airline("DVA"), 129, 1);
        _info.addFlight(fr);
        assertEquals(2, _info.getFlights().size());
        assertEquals(_info.getPilotID(), fr.getDatabaseID(FlightReport.DBID_PILOT));
    }
}
