package org.deltava.beans.gallery;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.Person;
import org.deltava.beans.Pilot;
import org.hansel.CoverageDecorator;

public class TestVote extends AbstractBeanTestCase {

    private Vote _v;
    
    public static Test suite() {
        return new CoverageDecorator(TestVote.class, new Class[] { Vote.class });
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        _v = null;
        super.tearDown();
    }

    public void testProperties() {
        _v = new Vote(2, 0, 1);
        setBean(_v);
        checkProperty("score", new Integer(5));
        assertEquals(1, _v.getImageID());
        assertEquals(2, _v.getAuthorID());
    }
    
    public void testPerson() {
        Person p = new Pilot("John", "Smith");
        p.setID(123);
        
        _v = new Vote(p, 0, 1);
        assertEquals(123, _v.getAuthorID());
        try {
            _v = new Vote(null, 0, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {  }
    }
    
    public void testValidation() {
        _v = new Vote(1, 0, 1);
        setBean(_v);
        validateInput("score", new Integer(-1), IllegalArgumentException.class);
        validateInput("score", new Integer(11), IllegalArgumentException.class);
        validateInput("authorID", new Integer(0), IllegalArgumentException.class);
        validateInput("imageID", new Integer(0), IllegalArgumentException.class);
    }
}