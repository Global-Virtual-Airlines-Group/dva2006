package org.deltava.jdbc;

import java.io.*;
import java.sql.*;

import java.util.Properties;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class TestConnectionPool extends TestCase {

    private ConnectionPool _pool;
    private Properties _props;
    
    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("etc/log4j.properties");
        _props = new Properties();
        _props.load(new FileInputStream("data/jdbc.properties"));
        _pool = new ConnectionPool(2);
    }

    protected void tearDown() throws Exception {
        _pool.close();
        _props = null;
        super.tearDown();
    }
    
    public void testProperties() throws ClassNotFoundException {
        assertEquals(-1, _pool.getSize());
        assertEquals(2, _pool.getMaxSize());
        _pool.setProperties(_props);
        _pool.setCredentials(_props.getProperty("user"), _props.getProperty("password"));
        _pool.setDriver(_props.getProperty("driver"));
    }
    
    public void testValidation() throws Exception {
        try {
            _pool.setDriver("java.foo.bar");
            fail("ClassNotFoundException expected");
        } catch (ClassNotFoundException cnfe) { }
        
        try {
            _pool.getConnection();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        try {
            _pool.connect(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { }
        
        try {
            _pool.connect(3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { }
    }
    
    public void testSemaphore() {
    	Semaphore lock = new Semaphore(1, true);
    	assertTrue(lock.tryAcquire());
    	assertFalse(lock.tryAcquire());
    	lock.release();
    	assertTrue(lock.tryAcquire());
    	lock.release();
    	lock.release();
    }
    
    public void testConnections() throws Exception {
        _pool.setProperties(_props);
        _pool.connect(1);
        assertEquals(1, _pool.getSize());
        Connection c1 = _pool.getConnection();
        assertNotNull(c1);
        Connection c2 = _pool.getConnection();
        assertEquals(2, _pool.getSize());
        assertEquals(_pool.getMaxSize(), _pool.getSize());
        try {
            Connection c3 = _pool.getConnection();
            fail("ConectionPoolException expected");
            assertNotNull(c3);
        } catch (ConnectionPoolException cpe) { }
        
        _pool.release(c2);
        assertEquals(2, _pool.getSize());
        Connection c3 = _pool.getConnection();
        c3.close();
        
        _pool.release(c3);
        _pool.close();
    }
}