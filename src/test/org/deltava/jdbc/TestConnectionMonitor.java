package org.deltava.jdbc;

import java.util.ArrayList;

import junit.framework.TestCase;

public class TestConnectionMonitor extends TestCase {

    private ConnectionMonitor _cm;
    
    protected void setUp() throws Exception {
        super.setUp();
        _cm = new ConnectionMonitor();
    }

    protected void tearDown() throws Exception {
        _cm = null;
        super.tearDown();
    }

    public void testBasicProperties() {
        assertEquals(0, _cm.size());
        assertEquals(5, _cm.getInterval());
        _cm.setInterval(60);
        assertEquals(60, _cm.getInterval());

        ArrayList pool1 = new ArrayList();
        pool1.add(new Object());
        _cm.setPool(pool1);
        assertEquals(pool1.size(), _cm.size());
        
        // Check to ensure that we have cloned pool1
        pool1.clear();
        assertEquals(0, pool1.size());
        assertEquals(1, _cm.size());
    }
    
    public void testMultiplePools() {
        ArrayList pool1 = new ArrayList();
        pool1.add(new Object());
        
        ArrayList pool2 = new ArrayList();
        pool2.add(new Object());
        pool2.add(new Object());
        
        ConnectionMonitor cm = new ConnectionMonitor(pool1);
        assertEquals(pool1.size(), cm.size());
        
        cm.addPool(pool2);
        
        assertEquals(pool1.size() + pool2.size(), cm.size());
    }
    
    public void testThreadExecution() throws Exception {
        assertEquals(0, _cm.size());
        _cm.setInterval(1);
        assertFalse(_cm.isAlive());
        assertTrue(_cm.isDaemon());
        
        _cm.start();
        assertTrue(_cm.isAlive());
        
        _cm.interrupt();
        _cm.join(2000);
    }
}

