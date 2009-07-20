package org.deltava.beans.schedule;

import org.hansel.CoverageDecorator;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;

public class TestGeoPosition extends AbstractBeanTestCase {
    
    private GeoPosition _gp;
    
    public static Test suite() {
        return new CoverageDecorator(TestGeoPosition.class, new Class[] { GeoPosition.class } );
    }

    protected void setUp() throws Exception {
        _gp = new GeoPosition();
        setBean(_gp);
    }

    protected void tearDown() throws Exception {
        _gp = null;
        super.tearDown();
    }

    public void testConstructor() {
        assertEquals(0, _gp.getLatitude(), 0);
        assertEquals(0, _gp.getLongitude(), 0);
        
        _gp = new GeoPosition(45.123, -64.234);
        assertNotNull(_gp);
        assertEquals(45.123, _gp.getLatitude(), 0.00001);
        assertEquals(-64.234, _gp.getLongitude(), 0.00001);
    }
    
    public void testEquality() {
        _gp.setLatitude(10, 15, 20);
        GeoPosition gp2 = new GeoPosition(_gp.getLatitude(), _gp.getLongitude());
        assertTrue(_gp.equals(gp2));
        assertFalse(_gp.equals(new Object()));
        
        GeoPosition gp3 = new GeoPosition(_gp.getLatitude() + 0.1, _gp.getLongitude());
        assertFalse(_gp.equals(gp3));
    }
    
    public void testHelpers() {
        _gp.setLatitude(10, 15, 20);
        assertEquals(10, GeoPosition.getDegrees(_gp.getLatitude()));
        assertEquals(15, GeoPosition.getMinutes(_gp.getLatitude()));
        assertEquals(20.0, GeoPosition.getSeconds(_gp.getLatitude()), 0.001);

        // Ensure m/s are always positive even if the entire number isn't
        _gp.setLatitude(-10, 15, 20);
        assertTrue((_gp.getLatitude() < 0));
        assertEquals(-10, GeoPosition.getDegrees(_gp.getLatitude()));
        assertEquals(15, GeoPosition.getMinutes(_gp.getLatitude()));
        assertEquals(20.0, GeoPosition.getSeconds(_gp.getLatitude()), 0.001);
        
        _gp.setLongitude(10, 15, 20);
        assertEquals(10, GeoPosition.getDegrees(_gp.getLongitude()));
        assertEquals(15, GeoPosition.getMinutes(_gp.getLongitude()));
        assertEquals(20, GeoPosition.getSeconds(_gp.getLongitude()), 0.001);
        
        // Ensure m/s are always positive even if the entire number isn't
        _gp.setLongitude(-10, 15, 20);
        assertTrue((_gp.getLongitude() < 0));
        assertEquals(-10, GeoPosition.getDegrees(_gp.getLongitude()));
        assertEquals(15, GeoPosition.getMinutes(_gp.getLongitude()));
        assertEquals(20, GeoPosition.getSeconds(_gp.getLongitude()), 0.001);
    }
    
    public void testMidPoint() {
       GeoPosition nyc = new GeoPosition(40.66972222, -73.94388889);
       GeoPosition lax = new GeoPosition(34.122222, -118.4111111);
       
       GeoPosition mp = lax.midPoint(nyc);
       assertEquals(39.54707861, mp.getLatitude(), 0.0001);
       assertEquals(-97.201534, mp.getLongitude(), 0.0001);
       
       GeoPosition mp2 = nyc.midPoint(lax);
       assertEquals(39.54707861, mp2.getLatitude(), 0.0001);
       assertEquals(-97.201534, mp2.getLongitude(), 0.0001);
    }
    
    public void testValidation() {
       validateInput("latitude", new Double(-90.0001), IllegalArgumentException.class);
       validateInput("latitude", new Double(90.0001), IllegalArgumentException.class);
        
        try {
            _gp.setLatitude(90, 0, 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }

        try {
            _gp.setLatitude(-90, 0, 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { 
        	// empty
        }
    }
}