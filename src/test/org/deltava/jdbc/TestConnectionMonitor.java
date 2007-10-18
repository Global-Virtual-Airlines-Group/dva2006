package org.deltava.jdbc;

import java.util.ArrayList;

import junit.framework.TestCase;

public class TestConnectionMonitor extends TestCase {

    private ConnectionMonitor _cm;
    
    protected void setUp() throws Exception {
        super.setUp();
        _cm = new ConnectionMonitor(3, new ConnectionPool(1));
    }

    protected void tearDown() throws Exception {
        _cm = null;
        super.tearDown();
    }

    public void testBasicProperties() {
        assertEquals(0, _cm.size());

        ArrayList<Object> pool1 = new ArrayList<Object>();
        pool1.add(new Object());
        assertEquals(pool1.size(), _cm.size());
        
        // Check to ensure that we have cloned pool1
        pool1.clear();
        assertEquals(0, pool1.size());
        assertEquals(1, _cm.size());
    }
    
    public void testMultiplePools() {
        ArrayList<Object> pool1 = new ArrayList<Object>();
        pool1.add(new Object());
        
        ArrayList<Object> pool2 = new ArrayList<Object>();
        pool2.add(new Object());
        pool2.add(new Object());
    }
    
    public void testThreadExecution() throws Exception {
    	Thread t = new Thread(_cm);
    	t.setDaemon(true);
        assertEquals(0, _cm.size());
        assertFalse(t.isAlive());
        assertTrue(t.isDaemon());
        
        t.start();
        assertTrue(t.isAlive());
        
        t.interrupt();
        t.join(2000);
    }
}