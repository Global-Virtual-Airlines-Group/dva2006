package org.deltava.beans;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

public class TestStaff extends AbstractBeanTestCase {
    
    private Staff _s;
    
    public static Test suite() {
        return new CoverageDecorator(TestStaff.class, new Class[] { Staff.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _s = new Staff("John", "Smith");
        setBean(_s);
        _s.setSortOrder(5);
    }
    
    protected void tearDown() throws Exception {
        _s = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("John", _s.getFirstName());
        assertEquals("Smith", _s.getLastName());
        checkProperty("title", "CEO");
        checkProperty("sortOrder", new Integer(2));
        checkProperty("EMail", "luke@sce.net");
        checkProperty("body", "John is a pilot.");
        checkProperty("ID", new Integer(8012));
    }
    
    public void testValidation() {
        validateInput("sortOrder", new Integer(0), IllegalArgumentException.class);
        validateInput("title", null, NullPointerException.class);
        validateInput("ID", new Integer(0), IllegalArgumentException.class);
        try {
            Staff s2 = new Staff(null, "Smith");
            fail("NullPointerException expected");
            assertNull(s2);
        } catch (NullPointerException npe) {
        	// empty
        }
        
        try {
            Staff s2 = new Staff("John", null);
            fail("NullPointerException expected");
            assertNull(s2);
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testComparator() {
        Staff s2 = new Staff("Jim", "Smith");
        s2.setSortOrder(3);
        Staff s3 = new Staff("Luke", "Kolin");
        s3.setSortOrder(5);
        assertTrue(s2.compareTo(_s) < 0);
        assertTrue(_s.compareTo(s2) > 0);
        assertTrue(s3.compareTo(_s) < 0);
    }
}