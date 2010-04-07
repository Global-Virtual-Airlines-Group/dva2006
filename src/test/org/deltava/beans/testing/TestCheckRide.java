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
        checkProperty("ID", Integer.valueOf(123));
        checkProperty("pilotID", Integer.valueOf(123));
        checkProperty("scorerID", Integer.valueOf(123));
        checkProperty("firstName", "John");
        checkProperty("lastName", "Smith");
        checkProperty("score", Integer.valueOf(1));
        checkProperty("score", Integer.valueOf(0));
        checkProperty("stage", Integer.valueOf(3));
        checkProperty("size", Integer.valueOf(3124));
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
        validateInput("ID", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("ID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("pilotID", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("pilotID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("scorerID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("score", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("score", Integer.valueOf(51), IllegalArgumentException.class);
        validateInput("score", Integer.valueOf(101), IllegalArgumentException.class);
        validateInput("stage", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("size", Integer.valueOf(-1), IllegalArgumentException.class);
        try {
            CheckRide cr2 = new CheckRide(null);
            assertNotNull(cr2);
            fail("NullPointerException expected");
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