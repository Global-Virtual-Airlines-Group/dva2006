package org.deltava.beans.testing;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestCheckRide extends AbstractBeanTestCase {

    private CheckRide _cr;
    
    public static Test suite() {
        return new CoverageDecorator(TestCheckRide.class, new Class[] { CheckRide.class } );
    }
    
    protected void tearDown() throws Exception {
        _cr = null;
        super.tearDown();
    }

    public void testProperties() {
        _cr = new CheckRide("Concorde Video");
        assertEquals("Concorde Video", _cr.getName());
        assertEquals(org.deltava.beans.testing.Test.CHECKRIDE, _cr.getType());
        setBean(_cr);
        checkProperty("fileName", "video.vid");
        checkProperty("ID", new Integer(123));
        checkProperty("pilotID", new Integer(123));
        checkProperty("scorerID", new Integer(123));
        checkProperty("firstName", "John");
        checkProperty("lastName", "Smith");
        checkProperty("score", new Integer(1));
        checkProperty("score", new Integer(0));
        checkProperty("stage", new Integer(3));
        checkProperty("size", new Integer(3124));
        checkProperty("date", new Date());
        checkProperty("submittedOn", new Date());
        checkProperty("scoredOn", new Date());
        checkProperty("comments", "Test Comments");
        assertFalse(_cr.getPassFail());
        _cr.setScore(true);
        assertTrue(_cr.getPassFail());
        assertEquals(1, _cr.getScore());
    }
    
    public void testCheckRide() {
        _cr = new CheckRide("Concorde Checkride");
        assertEquals("Concorde Checkride", _cr.getName());
        assertEquals(org.deltava.beans.testing.Test.CHECKRIDE, _cr.getType());
    }
    
    public void testValidation() {
        _cr = new CheckRide("Concorde Video");
        setBean(_cr);
        validateInput("ID", new Integer(0), IllegalArgumentException.class);
        validateInput("ID", new Integer(-1), IllegalArgumentException.class);
        validateInput("pilotID", new Integer(0), IllegalArgumentException.class);
        validateInput("pilotID", new Integer(-1), IllegalArgumentException.class);
        validateInput("scorerID", new Integer(-1), IllegalArgumentException.class);
        validateInput("score", new Integer(-1), IllegalArgumentException.class);
        validateInput("score", new Integer(51), IllegalArgumentException.class);
        validateInput("score", new Integer(101), IllegalArgumentException.class);
        validateInput("stage", new Integer(-1), IllegalArgumentException.class);
        validateInput("size", new Integer(-1), IllegalArgumentException.class);
        try {
            CheckRide cr2 = new CheckRide(null);
            fail("NullPointerException expected");
            assertNotNull(cr2);
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        _cr = new CheckRide("Concorde Video");
        _cr.setScore(true);
        _cr.setDate(new Date(now));
        
        CheckRide cr2 = new CheckRide("Concorde Video");
        cr2.setScore(false);
        cr2.setDate(new Date(now - 864000));
        
        assertTrue(_cr.compareTo(cr2) > 0);
        assertTrue(cr2.compareTo(_cr) < 0);
    }
}