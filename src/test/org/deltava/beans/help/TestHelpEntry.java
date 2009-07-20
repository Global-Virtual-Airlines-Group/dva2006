package org.deltava.beans.help;

import org.deltava.beans.help.OnlineHelpEntry;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestHelpEntry extends TestCase {

    private OnlineHelpEntry _he;
    
    public static Test suite() {
        return new CoverageDecorator(TestHelpEntry.class, new Class[] { OnlineHelpEntry.class } );
    }
    
    protected void tearDown() throws Exception {
        _he = null;
        super.tearDown();
    }

    public void testHelpEntry() {
        _he = new OnlineHelpEntry("SUBJ", "BODY");
        assertEquals("SUBJ", _he.getTitle());
        assertEquals("BODY", _he.getBody());
        assertEquals(_he.getTitle().hashCode(), _he.hashCode());
    }
    
    public void testException() {
        try {
            _he = new OnlineHelpEntry(null, "BODY");
            fail("Expected NullPointerException");
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testComparator() {
        _he = new OnlineHelpEntry("SUBJ", "BODY");
        OnlineHelpEntry he2 = new OnlineHelpEntry("SUBJ", "BODY2");
        OnlineHelpEntry he3 = new OnlineHelpEntry("SUBJECT", "BODY2");
        
        assertTrue(_he.compareTo(he3) < 0);
        assertEquals(0, _he.compareTo(he2));
        assertTrue(_he.equals(he2));
        assertFalse(_he.equals(he3));
        assertFalse(_he.equals(new Object()));
        assertFalse(_he.equals(null));
    }
}