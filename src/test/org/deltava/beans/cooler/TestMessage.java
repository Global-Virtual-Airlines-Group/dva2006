package org.deltava.beans.cooler;

import java.time.Instant;
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

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _p = new Pilot("John", "Smith");
        _p.setID(123);
        _msg = new Message(_p.getID());
        setBean(_msg);
    }

    @Override
	protected void tearDown() throws Exception {
        _msg = null;
        _p = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals(_p.getID(), _msg.getAuthorID());
        checkProperty("createdOn", Instant.now());
        checkProperty("remoteAddr", "127.0.0.1");
        checkProperty("remoteHost", "localhost");
        checkProperty("threadID", Integer.valueOf(1234));
        checkProperty("body", "Message Body");
    }
    
    public void testValidation() {
        validateInput("threadID", Integer.valueOf(0), IllegalArgumentException.class);
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        Message m2 = new Message(_p.getID());
        m2.setCreatedOn(Instant.ofEpochMilli(now + 1000));
        assertTrue(_msg.compareTo(m2) < 0);
        assertTrue(m2.compareTo(_msg) > 0);
    }
}