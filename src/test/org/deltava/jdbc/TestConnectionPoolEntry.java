package org.deltava.jdbc;

import java.io.*;
import java.sql.*;
import java.util.*;

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
        
        assertFalse(_cpe.isDynamic());
        _cpe.setDynamic(true);
        assertTrue(_cpe.isDynamic());
        _cpe.setDynamic(false);
        assertFalse(_cpe.isDynamic());
        
        Connection c2 = _cpe.reserve(false);
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
        assertTrue(_cpe.isDynamic());
        _cpe.setDynamic(false);
        assertTrue(_cpe.isDynamic());
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
    
    public void testIndexOf() {
       ConnectionPoolEntry cpe2 = new ConnectionPoolEntry(2, _props.getProperty("url"), _props);
       List<ConnectionPoolEntry> l = new ArrayList<ConnectionPoolEntry>();
       l.add(_cpe);
       l.add(cpe2);
       assertEquals(0, l.indexOf(_cpe));
       assertEquals(1, l.indexOf(cpe2));
    }
    
    public void testValidation() {
        _cpe.reserve(false);
        try {
            _cpe.reserve(false);
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
        
        _cpe.free();
    }
}