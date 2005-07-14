package org.deltava.beans.system;

import org.deltava.beans.system.HelpEntry;
import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestHelpEntry extends TestCase {

    private HelpEntry _he;
    
    public static Test suite() {
        return new CoverageDecorator(TestHelpEntry.class, new Class[] { HelpEntry.class } );
    }
    
    protected void tearDown() throws Exception {
        _he = null;
        super.tearDown();
    }

    public void testHelpEntry() {
        _he = new HelpEntry("SUBJ", "BODY");
        assertEquals("SUBJ", _he.getTitle());
        assertEquals("BODY", _he.getBody());
        assertEquals(_he.getTitle().hashCode(), _he.hashCode());
    }
    
    public void testException() {
        try {
            _he = new HelpEntry(null, "BODY");
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) { }
    }
    
    public void testComparator() {
        _he = new HelpEntry("SUBJ", "BODY");
        HelpEntry he2 = new HelpEntry("SUBJ", "BODY2");
        HelpEntry he3 = new HelpEntry("SUBJECT", "BODY2");
        
        assertTrue(_he.compareTo(he3) < 0);
        assertEquals(0, _he.compareTo(he2));
        assertTrue(_he.equals(he2));
        assertFalse(_he.equals(he3));
        assertFalse(_he.equals(new Object()));
        assertFalse(_he.equals(null));
    }
}