package org.deltava.jdbc;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

public class TestConnectionPoolEntry extends TestCase {

    private Connection _c;
    private ConnectionPoolEntry _cpe;
    private Properties _props;
    
    public static Test suite() {
        return new CoverageDecorator(TestConnectionPoolEntry.class, new Class[] { ConnectionPoolEntry.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _props = new Properties();
        _props.load(new FileInputStream("data/jdbc.properties"));
        Class.forName(_props.getProperty("driver"));
        _cpe = new ConnectionPoolEntry(1, _props.getProperty("url"), _props);
        _cpe.connect();
        _c = _cpe.getConnection();
    }

    protected void tearDown() throws Exception {
        _cpe.close();
        _c = null;
        _cpe = null;
        _props = null;
        super.tearDown();
    }

    public void testProperties() throws InterruptedException {
        assertFalse(_cpe.inUse());
        assertEquals(1, _cpe.getID());
        assertEquals(_c.hashCode(), _cpe.hashCode());
        assertSame(_c, _cpe.getConnection());
        
        assertFalse(_cpe.isRestartable());
        _cpe.setRestartable(true);
        assertTrue(_cpe.isRestartable());
        _cpe.setRestartable(false);
        assertFalse(_cpe.isRestartable());
        
        Connection c2 = _cpe.reserve();
        assertSame(c2, _c);
        assertTrue(_cpe.inUse());
        Thread.sleep(100);
        _cpe.free();
        _cpe.free(); // Should not fail
        
        long useTime = _cpe.getUseTime();
        assertEquals(useTime, _cpe.getTotalUseTime());
    }
    
    public void testSystemConnection() {
        assertFalse(_cpe.inUse());
        assertFalse(_cpe.isSystemConnection());
        _cpe.setSystemConnection(true);
        assertTrue(_cpe.isSystemConnection());
        assertTrue(_cpe.isRestartable());
        _cpe.setRestartable(false);
        assertTrue(_cpe.isRestartable());
    }
    
    public void testReconnection() throws SQLException {
        _cpe.close();
        _cpe.connect();
        try {
            _cpe.connect();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
    }
    
    public void testCloseException() {
        ConnectionPoolEntry cpe2 = new ConnectionPoolEntry(2, _props.getProperty("url"), _props);
        cpe2.close();
    }
    
    public void testEquality() throws SQLException {
        Connection c2 = DriverManager.getConnection(_props.getProperty("url"), _props);
        assertTrue(_cpe.equals(_c));
        assertFalse(_cpe.equals(c2));
        assertFalse(_cpe.equals(new Object()));
        assertFalse(_cpe.equals(null));
    }
    
    public void testValidation() {
        _cpe.reserve();
        try {
            _cpe.reserve();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        _cpe.free();
    }
}