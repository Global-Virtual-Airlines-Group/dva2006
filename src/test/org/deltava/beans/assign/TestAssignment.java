package org.deltava.beans.assign;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

public class TestAssignment extends AbstractBeanTestCase {

    private AssignmentLeg _a;
    
    public static Test suite() {
        return new CoverageDecorator(TestAssignment.class, new Class[] { AssignmentLeg.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _a = new AssignmentLeg(new Airline("DVA", "Delta Virtual Airlines"), 123, 1);
        setBean(_a);
    }

    protected void tearDown() throws Exception {
        _a = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("DVA", _a.getAirline().getCode());
        assertEquals(123, _a.getFlightNumber());
        assertEquals(1, _a.getLeg());
    }
    
    public void testLength() {
        try {
            assertEquals(0, _a.getLength());
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException uoe) {
        	// empty
        }
    }
    
    public void testFlightConstructor() {
       _a.setEquipmentType("CV-880");
       _a.setAirportD(new Airport("ATL", "KATL", "Atlanta GA"));
       _a.setAirportA(new Airport("LAX", "KLAX", "Los Angeles CA"));
       
       AssignmentLeg al2 = new AssignmentLeg(_a);
       assertEquals(_a.getAirline(), al2.getAirline());
       assertEquals(_a.getFlightNumber(), al2.getFlightNumber());
       assertEquals(_a.getLeg(), al2.getLeg());
       assertEquals(_a.getEquipmentType(), al2.getEquipmentType());
       assertEquals(_a.getAirportD(), al2.getAirportD());
       assertEquals(_a.getAirportA(), al2.getAirportA());
    }
}