package org.deltava.beans.cooler;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.Pilot;

public class TestMessage extends AbstractBeanTestCase {
    
    private Message _msg;
    private Pilot _p;
    
    public static Test suite() {
        return new CoverageDecorator(TestMessage.class, new Class[] { Message.class } );
    }

    protected void setUp() throws Exception {
        super.setUp();
        _p = new Pilot("John", "Smith");
        _p.setID(123);
        _msg = new Message(_p.getID());
        setBean(_msg);
    }

    protected void tearDown() throws Exception {
        _msg = null;
        _p = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals(_p.getID(), _msg.getAuthorID());
        checkProperty("createdOn", new Date());
        checkProperty("remoteAddr", "127.0.0.1");
        checkProperty("remoteHost", "localhost");
        checkProperty("threadID", new Integer(1234));
        checkProperty("body", "Message Body");
    }
    
    public void testValidation() {
        validateInput("threadID", new Integer(0), IllegalArgumentException.class);
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        Message m2 = new Message(_p.getID());
        m2.setCreatedOn(new Date(now + 1000));
        assertTrue(_msg.compareTo(m2) < 0);
        assertTrue(m2.compareTo(_msg) > 0);
    }
}