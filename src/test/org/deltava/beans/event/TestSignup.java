package org.deltava.beans.event;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.schedule.Airport;

public class TestSignup extends AbstractBeanTestCase {

    private Signup _s;
    
    public static Test suite() {
        return new CoverageDecorator(TestSignup.class, new Class[] { Signup.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _s = new Signup(123, 1234);
        setBean(_s);
    }

    protected void tearDown() throws Exception {
        _s = null;
        super.tearDown();
    }

    public void testProperties() {
        Airport atl = new Airport("ATL", "KATL", "Atlanta GA");
        Airport jfk = new Airport("JFK", "KJFK", "New York-Kennedy NY");
        
        assertEquals(123, _s.getID());
        assertEquals(1234, _s.getPilotID());
        checkProperty("remarks", "REMARKS");
        checkProperty("equipmentType", "B737-800");
        checkProperty("ID", new Integer(123));
        checkProperty("airportD", atl);
        checkProperty("airportA", jfk);
    }
    
    public void testValidation() {
        validateInput("eventID", new Integer(0), IllegalArgumentException.class);
        validateInput("pilotID", new Integer(0), IllegalArgumentException.class);
    }
}